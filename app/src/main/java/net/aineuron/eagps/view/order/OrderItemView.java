package net.aineuron.eagps.view.order;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import net.aineuron.eagps.Appl;
import net.aineuron.eagps.R;
import net.aineuron.eagps.model.database.order.LocalPhotos;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.Photo;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;

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

		if (order.getTimeCreated() != null) {
			date.setText(Appl.dateFormat.format(order.getTimeCreated()));
		}
		if (order.getClaimSaxCode() != null) {
			orderId.setText(order.getClaimSaxCode());
		}
		if (order.getClientCarLicencePlate() != null) {
			licensePlate.setText(order.getClientCarLicencePlate());
		}

        boolean hasDocuments = false;
        if (order.getOrderSheetProvided() != null) {
            hasDocuments = order.getOrderSheetProvided();
        }
        boolean hasPhotos = false;
        if (order.getPhotosProvided() != null) {
            hasPhotos = order.getPhotosProvided();
        }

        Realm db = RealmHelper.getDb();
        LocalPhotos localPhotos = db.where(LocalPhotos.class).equalTo("orderId", order.getId()).findFirst();

        if (localPhotos != null) {
            for (Photo photo : localPhotos.getLocalPhotos()) {
                if (photo.getType() == Photo.PHOTO_TYPE_PHOTO) {
                    hasPhotos = true;
                } else if (photo.getType() == Photo.PHOTO_TYPE_DOCUMENT) {
                    hasDocuments = true;
                }
            }
        } else {
            for (Photo photo : order.getPhotos()) {
                if (photo.getType() == Photo.PHOTO_TYPE_PHOTO) {
                    hasPhotos = true;
                } else if (photo.getType() == Photo.PHOTO_TYPE_DOCUMENT) {
                    hasDocuments = true;
                }
            }
        }

        if (hasDocuments) {
            documentsCheck.setImageResource(R.drawable.icon_check);
        } else {
            documentsCheck.setImageResource(R.drawable.icon_cross);
        }

        if (hasPhotos) {
            photosCheck.setImageResource(R.drawable.icon_check);
        } else {
            photosCheck.setImageResource(R.drawable.icon_cross);
        }
	}
}
