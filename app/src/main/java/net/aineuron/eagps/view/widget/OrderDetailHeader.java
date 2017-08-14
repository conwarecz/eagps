package net.aineuron.eagps.view.widget;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

import net.aineuron.eagps.R;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.IntentUtils;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 10-Jun-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.widget_order_detail_header)
public class OrderDetailHeader extends ConstraintLayout {

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

	private Order order;
	private OnClickListener detailClickListener;

	public OrderDetailHeader(Context context) {
		super(context);
	}

	public OrderDetailHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public OrderDetailHeader(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	public void setContent(Order order, OnClickListener detailClickListener) {
		this.order = order;
		this.detailClickListener = detailClickListener;
		setContent();
	}

	@Click(R.id.telephone)
	void telephoneClicked() {
		IntentUtils.dialPhone(getContext(), order.getClientPhone());
	}

	@Click(R.id.clientAddress)
	void clientAddressClicked() {
		IntentUtils.openMapLocation(getContext(), order.getClientAddress().getLocation(), order.getClientFirstName() + " " + order.getClientLastName());
	}

	@Click(R.id.destinationAddress)
	void setDestinationAddressClicked() {
		IntentUtils.openMapLocation(getContext(), order.getDestinationAddress().getAddress().getLocation(), order.getDestinationAddress().getName());
	}

	@Click(R.id.orderDetailButton)
	void orderDetailClicked() {
		if (detailClickListener != null) {
			detailClickListener.onClick(this);
		}
	}

	private void setContent() {
		if (order == null) {
			return;
		}

		this.claimNumber.setText(order.getClaimSaxCode());
		this.clientName.setText(order.getClientFirstName() + order.getClientLastName());
		this.telephone.setText(order.getClientPhone());
		this.clientCar.setText(order.getClientCarModel() + ", " + order.getClientCarWeight());
		this.licensePlate.setText(order.getClientLicencePlate());
		this.limit.setText(order.getLimitation().getLimit());
		this.limit.setExtendedDescription(order.getLimitation().isExtendedDescription());
	}
}
