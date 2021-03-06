package net.aineuron.eagps.fragment;

import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.activity.StateSettingsActivity_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.event.network.order.OrderFinalizedEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.FormatUtil;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.NetworkUtil;
import net.aineuron.eagps.util.OrderToastComposer;
import net.aineuron.eagps.util.RealmHelper;
import net.aineuron.eagps.view.widget.IcoLabelTextView;
import net.aineuron.eagps.view.widget.OrderDetailHeader;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.realm.Realm;
import io.realm.RealmResults;

import static net.aineuron.eagps.activity.MainActivityBase.MAIN_TAB_ID;
import static net.aineuron.eagps.model.UserManager.DISPATCHER_ID;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY_ORDER;
import static net.aineuron.eagps.model.UserManager.STATE_ID_READY;
import static net.aineuron.eagps.model.UserManager.WORKER_ID;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_ASSIGNED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_ENTITY_FINISHED;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_tow)
public class TowFragment extends BaseFragment {

	@Bean
	UserManager userManager;

	@Bean
	OrdersManager ordersManager;

	@Bean
	ClientProvider clientProvider;

	@FragmentArg
	Long orderId;

	@ViewById(R.id.photosStep)
	IcoLabelTextView photosStep;

	@ViewById(R.id.documentPhotos)
	IcoLabelTextView documentPhotos;

	@ViewById(R.id.clientAddress)
	IcoLabelTextView clientAddress;

	@ViewById(R.id.destinationAddress)
	IcoLabelTextView destinationAddress;

	@ViewById(R.id.orderDetailHeader)
	OrderDetailHeader orderDetailHeader;

	private Order order;
	private Realm db;
	private RealmResults<Order> orderQuery;
	private boolean alreadyBacked = false;

	public static TowFragment newInstance(Long orderId) {
		return TowFragment_.builder().orderId(orderId).build();
	}

	@AfterViews
	void afterViews() {
		User user = userManager.getUser();
		if (user == null) {
			return;
		}
		setAppbarUpNavigation(user.getUserRole() == DISPATCHER_ID);
		if (userManager.getSelectedStateId() == STATE_ID_BUSY) {
			setAppbarTitle(getString(R.string.car_busy));
		} else if (userManager.getSelectedStateId() == STATE_ID_BUSY_ORDER) {
			setAppbarTitle(getString(R.string.car_on_order));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		alreadyBacked = false;
		dismissProgress();
		loadOrder();
	}

	@Override
	public void onPause() {
		removeListener();
		dismissProgress();
		alreadyBacked = true;
		super.onPause();
	}

	@Click(R.id.finishOrder)
	void finishClicked() {
		removeListener();
		alreadyBacked = true;
		showProgress("Dokončuji zakázku", getString(R.string.dialog_wait_content));
		clientProvider.getEaClient().finalizeOrder(order.getId());
	}

	@Click(R.id.cancelOrder)
	void cancelClicked() {
		new MaterialDialog.Builder(getContext())
				.title("Důvod zrušení")
				.items(R.array.order_cancel_choices)
				.itemsIds(R.array.order_cancel_choice_ids)
				.autoDismiss(false)
				.itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
					if (which >= 0) {
						removeListener();
						alreadyBacked = true;
						showProgress("Ruším zakázku", getString(R.string.dialog_wait_content));
						ordersManager.cancelOrder(order.getId(), (long) which + 1);
						dialog.dismiss();
					}
					return true;
				})
				.onPositive((dialog, which) -> {
					if (dialog.getSelectedIndex() < 0) {
						Toast.makeText(getContext(), "Vyberte důvod", Toast.LENGTH_SHORT).show();
					} else {
						dialog.dismiss();
					}
				})
				.positiveText("OK")
				.show();
	}

	@Click({R.id.photosStep, R.id.documentPhotos})
	void photosClicked() {
		IntentUtils.openCamera(getContext());
	}

	@Click(R.id.clientAddress)
	void clientAddressClicked() {
		IntentUtils.openMapLocation(getContext(), order.getClientAddress().getLocation(), order.getClientFirstName() + " " + order.getClientLastName());
	}

	@Click(R.id.destinationAddress)
	void setDestinationAddressClicked() {
		IntentUtils.openMapLocation(getContext(), order.getDestinationAddress().getLocation(), order.getWorkshopName());
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onOrderCanceledEvent(OrderCanceledEvent e) {
		dismissProgress();
		removeListener();
		if (userManager.haveActiveOrder() && userManager.getUser().getRoleId() == WORKER_ID) {
			// TODO: ověřit, jestli je nutno dělat New
			IntentUtils.openNewMainActivity(getContext());
		} else {
			if (userManager.getUser().getRoleId() == WORKER_ID) {
				userManager.setStateReady();
			}
			IntentUtils.openMainActivity(getContext());
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void apiFailedEvent(ApiErrorEvent e) {
		dismissProgress();
		e.throwable.printStackTrace();
		Toast.makeText(getContext(), e.message, Toast.LENGTH_SHORT).show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onKnownErrorEvent(KnownErrorEvent e) {
		dismissProgress();
		Toast.makeText(getContext(), e.knownError.getMessage(), Toast.LENGTH_LONG).show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void orderFinalized(OrderFinalizedEvent e) {
		dismissProgress();
		removeListener();
		MainActivityBase activity = (MainActivityBase) getActivity();
		activity.selectTab(MAIN_TAB_ID);
		if (userManager.getUser().getRoleId() == WORKER_ID) {
			if (userManager.haveActiveOrder()) {
				IntentUtils.openNewMainActivity(getContext());
			} else {
				userManager.setStateReady();
				userManager.setSelectedStateId(STATE_ID_READY);
				activity.showFragment(StateFragment_.newInstance(), false);
				StateSettingsActivity_.intent(getContext()).start();
			}
		} else {
			activity.showFragment(OrderAttachmentsFragment_.newInstance(e.orderId));
		}
	}

	private void loadOrder() {
		if (orderId == null) {
			order = ordersManager.getFirstActiveOrder();
			if (order != null) {
				orderId = order.getId();
			} else {
				IntentUtils.openNewMainActivity(getContext());
			}
		} else {
			if (order == null || !order.getId().equals(orderId)) {
				order = ordersManager.getOrderByIdCopy(orderId);
			}
		}

		if (order != null) {
			setContent();
			setOrderListener();
			if (NetworkUtil.isConnected(getContext())) {
				showProgress("Načítám detail", getString(R.string.dialog_wait_content));
			}
			ordersManager.updateOrder(orderId);
		}
	}

	private void setContent() {
		orderDetailHeader.setContent(order, v -> {
			removeListener();
			MainActivityBase activity = (MainActivityBase) getActivity();
			activity.showFragment(OrderDetailFragment.newInstance(order.getId(), null));
		});

		if (order.getClientAddress() != null) {
			this.clientAddress.setVisibility(View.VISIBLE);
			this.clientAddress.setText(FormatUtil.formatClientAddress(order.getClientAddress(), order.getClientLocationComment()));
		} else {
			this.clientAddress.setVisibility(View.GONE);
		}
		if (order.getDestinationAddress() != null) {
			this.destinationAddress.setVisibility(View.VISIBLE);
			this.destinationAddress.setText(FormatUtil.formatDestinationAddress(order.getDestinationAddress(), order.getWorkshopName()));
		} else {
			this.destinationAddress.setVisibility(View.GONE);
		}
	}

	private void setOrderListener() {
		removeListener();
		db = RealmHelper.getDb();
		orderQuery = db.where(Order.class).equalTo("id", orderId).findAll();

		orderQuery.addChangeListener((orders, changeSet) -> {
			order = ordersManager.getOrderByIdCopy(orderId);
			if (order == null) {
				return;
			}
			if (order.getStatus() != ORDER_STATE_ASSIGNED && order.getStatus() != ORDER_STATE_ENTITY_FINISHED) {
				dismissProgress();
				if (!alreadyBacked) {
					alreadyBacked = true;
					// TODO: Switch fragment to State Fragment
					Toast.makeText(getContext(), OrderToastComposer.getOrderChangedToastMessage(getContext(), order.getStatus()), Toast.LENGTH_LONG).show();
					if (userManager.getUser().getRoleId() == WORKER_ID) {
						IntentUtils.openMainActivity(getContext());
					} else {
						getActivity().onBackPressed();
					}
				}
			} else if (orderDetailHeader != null) {
				setContent();
				dismissProgress();
			}
		});
	}

	private void removeListener() {
		if (orderQuery == null) {
			return;
		}
		try {
			orderQuery.removeAllChangeListeners();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
