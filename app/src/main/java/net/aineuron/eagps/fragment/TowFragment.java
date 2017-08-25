package net.aineuron.eagps.fragment;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.RealmHelper;
import net.aineuron.eagps.view.widget.IcoLabelTextView;
import net.aineuron.eagps.view.widget.OrderDetailHeader;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.annotations.Nullable;
import io.realm.ObjectChangeSet;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObjectChangeListener;

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

	@Nullable
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

	@EventBusGreenRobot
	EventBus bus;

	private Order order;
	private Realm db;
	private RealmObjectChangeListener objectListener;

	public static TowFragment newInstance(Long orderId) {
		return TowFragment_.builder().orderId(orderId).build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(false);
		setAppbarTitle("Na zásahu");

		if (orderId == null) {
			order = ordersManager.getCurrentOrder();
			setContent();
		} else {
			setOrderListener();
			showProgress("Načítám detail", getString(R.string.dialog_wait_content));
			clientProvider.getEaClient().getOrderDetail(orderId);
		}
	}

	@Click(R.id.finishOrder)
	void finishClicked() {
		MainActivityBase activity = (MainActivityBase) getActivity();
		activity.showFragment(OrderAttachmentsFragment.newInstance(order.getId()));
	}

	@Click(R.id.cancelOrder)
	void cancelClicked() {
		// TODO: Redo correctly with api call
		new MaterialDialog.Builder(getContext())
				.title("Důvod zrušení")
				.items(R.array.order_cancel_choices)
				.itemsIds(R.array.order_cancel_choice_ids)
				.itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
					showProgress("Ruším zakázku", getString(R.string.dialog_wait_content));
					ordersManager.cancelOrder(order.getId(), Long.valueOf(which));
					return true;
				})
				.onPositive((dialog, which) -> {
					if (dialog.getSelectedIndex() < 0) {
						Toast.makeText(getContext(), "Vyberte důvod", Toast.LENGTH_SHORT).show();
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
		hideProgress();
		userManager.setSelectedStateId(UserManager.STATE_ID_READY);
		IntentUtils.openMainActivity(getContext());
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void apiFailedEvent(ApiErrorEvent e) {
		e.throwable.printStackTrace();
		Toast.makeText(getContext(), "Nepovedlo se stáhnout detail" + e.throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
		hideProgress();
		getActivity().onBackPressed();
	}

	private void setContent() {
		orderDetailHeader.setContent(order, v -> {
			MainActivityBase activity = (MainActivityBase) getActivity();
			activity.showFragment(OrderDetailFragment.newInstance(null));
		});

		this.clientAddress.setText(formatClientAddress(order.getClientAddress()));
		this.destinationAddress.setText(formatDestinationAddress(order.getDestinationAddress(), order.getWorkshopName()));
	}

	private void setOrderListener() {
		order = ordersManager.getOrderById(orderId);
		objectListener = new RealmObjectChangeListener() {
			@Override
			public void onChange(RealmModel realmModel, ObjectChangeSet changeSet) {
				db = RealmHelper.getDb();
				order = ordersManager.getOrderById(orderId);
				if (orderDetailHeader != null) {
					setContent();
					hideProgress();
				}
			}
		};
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
