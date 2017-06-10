package net.aineuron.eagps.fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.activity.MainActivity_;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.DestinationAddress;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.view.widget.IcoLabelTextView;
import net.aineuron.eagps.view.widget.OrderDetailHeader;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

	public static TowFragment newInstance() {
		return TowFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarTitle("Na zásahu");

		order = ordersManager.getCurrentOrder();

		setContent();
	}

	@Click(R.id.finishOrder)
	void finishClicked() {
		MainActivityBase activity = (MainActivityBase) getActivity();
		activity.showFragment(OrderAttachmentsFragment.newInstance());
	}

	@Click(R.id.cancelOrder)
	void cancelClicked() {
		// TODO: Redo correctly with api call
		new MaterialDialog.Builder(getContext())
				.title("Důvod zrušení")
				.items(R.array.order_cancel_choices)
				.itemsIds(R.array.order_cancel_choice_ids)
				.itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
					showProgress("Ruším zakázku", "Prosím čekejte...");
					ordersManager.cancelOrder(order.getId());
					return true;
				})
				.positiveText("OK")
				.show();
	}

	@Click({R.id.photosStep, R.id.documentPhotos})
	void photosClicked() {
		IntentUtils.openCamera(getContext());
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onOrderCanceledEvent(OrderCanceledEvent e) {
		hideProgress();
		userManager.setSelectedStateId(UserManager.STATE_ID_READY);
		MainActivity_.intent(getContext()).start();
		getActivity().finish();
	}

	private void setContent() {
		orderDetailHeader.setContent(order, v -> {
			MainActivityBase activity = (MainActivityBase) getActivity();
			activity.showFragment(OrderDetailFragment.newInstance());
		});


		Address clientAddress = order.getClientAddress();
		DestinationAddress destinationAddress = order.getDestinationAddress();

		this.clientAddress.setText(clientAddress.getStreet() + ", " + clientAddress.getCity() + ", " + clientAddress.getZipCode());
		this.destinationAddress.setText(destinationAddress.getName() + ", " + destinationAddress.getAddress().getStreet() + ", " + destinationAddress.getAddress().getCity() + ", " + destinationAddress.getAddress().getZipCode());
	}
}
