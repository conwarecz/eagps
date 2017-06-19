package net.aineuron.eagps.fragment;

import android.widget.TextView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.ClientCar;
import net.aineuron.eagps.model.database.order.DestinationAddress;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.view.widget.IcoLabelTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 07-Jun-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_order_detail)
public class OrderDetailFragment extends BaseFragment {

	@ViewById(R.id.client)
	IcoLabelTextView client;
	@ViewById(R.id.clientCar)
	IcoLabelTextView clientCar;
	@ViewById(R.id.clientAddress)
	IcoLabelTextView clientAddress;
	@ViewById(R.id.destinationAddress)
	IcoLabelTextView destinationAddress;
	@ViewById(R.id.eventDescription)
	IcoLabelTextView eventDescription;
	@ViewById(R.id.limit)
	IcoLabelTextView limit;

	@ViewById(R.id.header)
	TextView header;

	@Bean
	OrdersManager ordersManager;

	private Order order;

	public static OrderDetailFragment newInstance() {
		return OrderDetailFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(true);
		setAppbarTitle("Detail");
		this.order = ordersManager.getCurrentOrder();

		setUi();
	}

	@Click(R.id.client)
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

	@Click(R.id.back)
	void onBackClicked() {
		getActivity().onBackPressed();
	}

	private void setUi() {

		this.header.setText("Detail objednávky " + order.getClaimNumber());

		this.client.setText(order.getClientName() + ", " + order.getClientPhone());

		ClientCar car = order.getCar();
		this.clientCar.setText(car.getModel() + ", " + car.getWeight() + " t, " + car.getLicensePlate());

		Address clientAddress = order.getClientAddress();
		this.clientAddress.setText(clientAddress.getStreet() + ", " + clientAddress.getCity() + ", " + clientAddress.getZipCode());

		DestinationAddress destinationAddress = order.getDestinationAddress();
		this.destinationAddress.setText(destinationAddress.getName() + ", " + destinationAddress.getAddress().getStreet() + ", " + destinationAddress.getAddress().getCity() + ", " + destinationAddress.getAddress().getZipCode());

		this.eventDescription.setText(order.getEventDescription());

		this.limit.setText(order.getLimitation().getLimit());
	}

}
