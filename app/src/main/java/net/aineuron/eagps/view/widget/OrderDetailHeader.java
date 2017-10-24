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
		IntentUtils.openMapLocation(getContext(), order.getDestinationAddress().getLocation(), order.getWorkshopName());
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

        if (order.getClaimSaxCode() != null) {
            this.claimNumber.setText(order.getClaimSaxCode());
        }
        String name = "";
        if (order.getClientFirstName() != null) {
            name = order.getClientFirstName();
        }
        if (order.getClientLastName() != null) {
            name = name + " " + order.getClientLastName();
        }
        this.clientName.setText(name);
        if (order.getClientPhone() != null) {
            this.telephone.setText(order.getClientPhone());
        }
        String car = "";
        if (order.getClientCarModel() != null) {
            car = order.getClientCarModel();
        }
        if (order.getClientCarWeight() != null) {
            car = car + ", " + order.getClientCarWeight();
        }
        this.clientCar.setText(car);
        if (order.getClientCarLicencePlate() != null) {
            this.licensePlate.setText(order.getClientCarLicencePlate());
        }
        if (order.getLimitation() != null && order.getLimitation().getLimit() != null) {
			this.limit.setText(order.getLimitation().getLimit());
		}
		if (order.getLimitation() != null) {
			this.limit.setExtendedDescription(order.getLimitation().isExtendedDescription());
		}
	}
}
