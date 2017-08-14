package net.aineuron.eagps.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.annotation.NonNull;
import android.widget.TextView;
import android.widget.Toast;

import net.aineuron.eagps.R;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.DestinationAddress;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.FormatUtil;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.view.widget.IcoLabelTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 07-Jun-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_order_detail)
public class OrderDetailFragment extends BaseFragment {

	@SystemService
	ClipboardManager clipboardManager;

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

	@LongClick({R.id.client, R.id.clientAddress, R.id.destinationAddress, R.id.clientCar, R.id.eventDescription, R.id.limit})
	void clickedToCopy(IcoLabelTextView view) {
		String text = view.getText();
		copyToClipboard(text);
	}

	@Click(R.id.back)
	void onBackClicked() {
		getActivity().onBackPressed();
	}

	private void setUi() {

		this.header.setText("Detail objednávky " + order.getClaimSaxCode());

		this.client.setText(order.getClientFirstName() + " " + order.getClientLastName() + ", " + order.getClientPhone());

		this.clientCar.setText(order.getClientCarModel() + ", " + order.getClientCarWeight() + ", " + order.getClientLicencePlate());

		Address clientAddress = order.getClientAddress();
		this.clientAddress.setText(formatClientAddress(clientAddress));

		DestinationAddress destinationAddress = order.getDestinationAddress();
		this.destinationAddress.setText(formatDestinationAddress(destinationAddress));

		this.eventDescription.setText(FormatUtil.formatEvent(order.getEventDescription()));

		this.limit.setText(order.getLimitation().getLimit());
	}

	@NonNull
	private String formatDestinationAddress(DestinationAddress destinationAddress) {
		return destinationAddress.getName() + ", " + destinationAddress.getAddress().getAddress().getStreet() + ", " + destinationAddress.getAddress().getAddress().getCity() + ", " + destinationAddress.getAddress().getAddress().getZipCode();
	}

	@NonNull
	private String formatClientAddress(Address clientAddress) {
		return clientAddress.getAddress().getStreet() + ", " + clientAddress.getAddress().getCity() + ", " + clientAddress.getAddress().getZipCode();
	}

	private void copyToClipboard(String text) {
		ClipData clip = ClipData.newPlainText("Client Address", text);
		clipboardManager.setPrimaryClip(clip);
		Toast.makeText(getContext(), "Kopírováno: " + text, Toast.LENGTH_SHORT).show();
	}

}
