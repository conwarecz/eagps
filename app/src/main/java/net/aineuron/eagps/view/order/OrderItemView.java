package net.aineuron.eagps.view.order;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import net.aineuron.eagps.Appl;
import net.aineuron.eagps.R;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.PhotoPathsWithReason;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 20-Jun-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.item_orders_order)
public class OrderItemView extends ConstraintLayout {

	@ViewById(R.id.date)
	TextView date;

	@ViewById(R.id.orderId)
	TextView orderId;

	@ViewById(R.id.licensePlate)
	TextView licensePlate;

	@ViewById(R.id.documentsCheck)
	ImageView documentsCheck;

	@ViewById(R.id.photosCheck)
	ImageView photosCheck;

	public OrderItemView(Context context) {
		super(context);
	}

	public OrderItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public OrderItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void bind(Order order) {
		if (order.getStatus() == Order.ORDER_STATE_CREATED) {
			this.setBackgroundColor(getContext().getResources().getColor(R.color.createdOrder));
		} else if (order.getStatus() == Order.ORDER_STATE_ASSIGNED || order.getStatus() == Order.ORDER_STATE_ARRIVED) {
			this.setBackgroundColor(getContext().getResources().getColor(R.color.busy));
		} else if (order.getStatus() == Order.ORDER_STATE_FINISHED) {
			this.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
		} else if (order.getStatus() == Order.ORDER_STATE_SENT) {
			this.setBackgroundColor(getContext().getResources().getColor(R.color.ready));
		} else {
			this.setBackgroundColor(getContext().getResources().getColor(R.color.grayOrder));
		}

		date.setText(Appl.dateFormat.format(order.getTimeCreated()));
		orderId.setText(order.getClaimSaxCode());
		licensePlate.setText(order.getClientCarLicencePlate());

        if (isPathEmpty(order.getOrderDocuments())) {
            documentsCheck.setImageResource(R.drawable.icon_cross);
        } else {
            documentsCheck.setImageResource(R.drawable.icon_check);
        }

		if (isPathEmpty(order.getPhotos())) {
            documentsCheck.setImageResource(R.drawable.icon_cross);
        } else {
            documentsCheck.setImageResource(R.drawable.icon_check);
        }
	}

	private boolean isPathEmpty(PhotoPathsWithReason paths) {
		return paths == null || paths.getPhotoPaths().size() == 0;
	}
}
