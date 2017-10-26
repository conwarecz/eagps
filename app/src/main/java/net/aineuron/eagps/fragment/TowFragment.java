package net.aineuron.eagps.fragment;

import android.support.annotation.NonNull;
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
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.NetworkUtil;
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

import io.realm.ObjectChangeSet;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObjectChangeListener;

import static net.aineuron.eagps.activity.MainActivityBase.MAIN_TAB_ID;
import static net.aineuron.eagps.model.UserManager.DISPATCHER_ID;
import static net.aineuron.eagps.model.UserManager.WORKER_ID;

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
	private RealmObjectChangeListener objectListener;

	public static TowFragment newInstance(Long orderId) {
		return TowFragment_.builder().orderId(orderId).build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(userManager.getUser().getUserRole() == DISPATCHER_ID);
		setAppbarTitle("Na zásahu");

		if (orderId == null) {
			order = ordersManager.getFirstActiveOrder();
			if (order != null) {
				orderId = order.getId();
			}
		} else {
			if (order == null || !order.getId().equals(orderId)) {
				order = ordersManager.getOrderById(orderId);
			}
		}

		if (order != null) {
			setContent();
			setOrderListener();
			if (NetworkUtil.isConnected(getContext())) {
				showProgress("Načítám detail", getString(R.string.dialog_wait_content));
			}
			clientProvider.getEaClient().getOrderDetail(orderId);
		}
	}

	@Click(R.id.finishOrder)
	void finishClicked() {
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
						showProgress("Ruším zakázku", getString(R.string.dialog_wait_content));
						ordersManager.cancelOrder(order.getId(), Long.valueOf(which));
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

	@Override
	public void onPause() {
		super.onPause();
		hideProgress();
		try {
			order.removeAllChangeListeners();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		hideProgress();
		IntentUtils.openMainActivity(getContext());
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void apiFailedEvent(ApiErrorEvent e) {
		e.throwable.printStackTrace();
		Toast.makeText(getContext(), "Nepovedlo se stáhnout detail: " + e.throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
		hideProgress();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void knownErrorEvent(KnownErrorEvent e) {
		Toast.makeText(getContext(), "Zadaný požadavek se nepovedlo zpracovat, zkontrolujte připojení", Toast.LENGTH_LONG);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void orderFinalized(OrderFinalizedEvent e) {
		hideProgress();
		MainActivityBase activity = (MainActivityBase) getActivity();
        activity.onTabSelected(MAIN_TAB_ID);
        if (userManager.getUser().getRoleId() == WORKER_ID) {
            StateSettingsActivity_.intent(getContext()).start();
        } else {
			// TODO: buď attachmentsFragment nebo seznam zakázek pro dispatchera
			activity.showFragment(OrderAttachmentsFragment.newInstance(e.orderId));
		}
	}

	private void setContent() {
		orderDetailHeader.setContent(order, v -> {
			MainActivityBase activity = (MainActivityBase) getActivity();
			activity.showFragment(OrderDetailFragment.newInstance(order.getId(), null));
		});

		if (order.getClientAddress() != null) {
			this.clientAddress.setVisibility(View.VISIBLE);
			this.clientAddress.setText(formatClientAddress(order.getClientAddress()));
		} else {
			this.clientAddress.setVisibility(View.GONE);
		}
		if (order.getWorkshopName() != null && order.getDestinationAddress() != null) {
			this.destinationAddress.setVisibility(View.VISIBLE);
			this.destinationAddress.setText(formatDestinationAddress(order.getDestinationAddress(), order.getWorkshopName()));
		} else {
			this.destinationAddress.setVisibility(View.GONE);
		}
	}

	private void setOrderListener() {
		order = ordersManager.getOrderById(orderId);
		objectListener = new RealmObjectChangeListener() {
			@Override
			public void onChange(RealmModel realmModel, ObjectChangeSet changeSet) {
				db = RealmHelper.getDb();
				order = ordersManager.getOrderById(orderId);
                if (orderDetailHeader != null && order != null) {
                    setContent();
                }
                hideProgress();
			}
		};
		order.removeAllChangeListeners();
		order.addChangeListener(objectListener);
	}

	// Building up addresses from what we have
	@NonNull
	private String formatDestinationAddress(Address destinationAddress, String workshopName) {
		String addressResult = "";
		if (order.getDestinationAddress() != null) {
			if (order.getWorkshopName() != null) {
				addressResult += order.getWorkshopName();
			}
			if (destinationAddress.getAddress().getStreet() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getStreet();
			}
			if (destinationAddress.getAddress().getCity() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getCity();
			}
			if (destinationAddress.getAddress().getZipCode() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getZipCode();
			}
			this.destinationAddress.setText(addressResult);
		}
		return addressResult;
	}

	@NonNull
	private String formatClientAddress(Address clientAddress) {
		String addressResult = "";
		// Building up addresses from what we have
		if (order.getClientAddress() != null) {
			if (clientAddress.getAddress().getStreet() != null) {
				addressResult += clientAddress.getAddress().getStreet();
			}
			if (clientAddress.getAddress().getCity() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += clientAddress.getAddress().getCity();
			}
			if (clientAddress.getAddress().getZipCode() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += clientAddress.getAddress().getZipCode();
			}
			this.clientAddress.setText(addressResult);
		}
		return addressResult;
	}
}
