package net.aineuron.eagps.fragment;

import com.afollestad.materialdialogs.MaterialDialog;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.activity.MainActivity_;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.DestinationAddress;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.viewmodel.OrdersManager;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.view.widget.IcoLabelTextButtonView;
import net.aineuron.eagps.view.widget.IcoLabelTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

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

	@ViewById(R.id.claimNumber)
	IcoLabelTextView claimNumber;

	@ViewById(R.id.clientName)
	IcoLabelTextView clientName;

	@ViewById(R.id.clientCar)
	IcoLabelTextView clientCar;

	@ViewById(R.id.limit)
	IcoLabelTextButtonView limit;

	@ViewById(R.id.telephone)
	IcoLabelTextView telephone;

	@ViewById(R.id.licensePlate)
	IcoLabelTextView licensePlate;

	@ViewById(R.id.clientAddress)
	IcoLabelTextView clientAddress;

	@ViewById(R.id.photosStep)
	IcoLabelTextView photosStep;

	@ViewById(R.id.destinationAddress)
	IcoLabelTextView destinationAddress;

	@ViewById(R.id.documentPhotos)
	IcoLabelTextView documentPhotos;


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
					userManager.setSelectedStateId(UserManager.STATE_ID_READY);
					MainActivity_.intent(getContext()).start();
					getActivity().finish();
					return true;
				})
				.positiveText("OK")
				.show();
	}

	@Click(R.id.telephone)
	void telephoneClicked() {
		IntentUtils.dialPhone(getContext(), order.getClientPhone());
	}

	@Click(R.id.clientAddress)
	void clientAddressClicked() {
		IntentUtils.openMapLocation(getContext(), order.getClientAddress().getLocation(), order.getClientName());
	}

	@Click(R.id.destinationAddress)
	void setDestinationAddressClicked() {
		IntentUtils.openMapLocation(getContext(), order.getDestinationAddress().getAddress().getLocation(), order.getDestinationAddress().getName());
	}

	@Click({R.id.photosStep, R.id.documentPhotos})
	void photosClicked() {
		IntentUtils.openCamera(getContext());
	}

	@Click(R.id.orderDetailButton)
	void orderDetailClicked() {
		MainActivityBase activity = (MainActivityBase) getActivity();
		activity.showFragment(OrderDetailFragment.newInstance());
	}

	private void setContent() {
		// TODO Fill the fields
		Address clientAddress = order.getClientAddress();
		DestinationAddress destinationAddress = order.getDestinationAddress();

		this.claimNumber.setText(order.getClaimNumber());
		this.clientName.setText(order.getClientName());
		this.telephone.setText(order.getClientPhone());
		this.clientCar.setText(order.getCar().getModel() + ", " + order.getCar().getWeight() + " t");
		this.licensePlate.setText(order.getCar().getLicensePlate());
		this.limit.setText(order.getLimitation().getLimit());
		this.limit.setExtendedDescription(order.getLimitation().isExtendedDescription());
		this.clientAddress.setText(clientAddress.getStreet() + ", " + clientAddress.getCity() + ", " + clientAddress.getZipCode());
		this.destinationAddress.setText(destinationAddress.getName() + ", " + destinationAddress.getAddress().getStreet() + ", " + destinationAddress.getAddress().getCity() + ", " + destinationAddress.getAddress().getZipCode());
	}
}
