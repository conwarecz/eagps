package net.aineuron.eagps.fragment;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.adapter.PhotoPathsWithReasonAdapter;
import net.aineuron.eagps.adapter.PhotoPathsWithReasonAdapter_;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.view.widget.OrderDetailHeader;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 07-Jun-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragnent_order_attachments)
public class OrderAttachmentsFragment extends BaseFragment {

	@ViewById(R.id.orderDetailHeader)
	OrderDetailHeader orderDetailHeader;

	@ViewById(R.id.orderDocumentsView)
	RecyclerView orderDocumentsView;

	@ViewById(R.id.orderPhotosView)
	RecyclerView orderPhotosView;

	@FragmentArg
	Long orderId;

	@Bean
	OrdersManager ordersManager;

	private Order order;

	public static OrderAttachmentsFragment newInstance(Long orderId) {
		return OrderAttachmentsFragment_.builder().orderId(orderId).build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(true);
		setAppbarTitle("Přílohy");

		order = ordersManager.getOrderById(orderId);

		setContent();
	}

	private void setContent() {
		orderDetailHeader.setContent(order, v -> {
			MainActivityBase activity = (MainActivityBase) getActivity();
			activity.showFragment(OrderDetailFragment.newInstance());
		});

		PhotoPathsWithReasonAdapter documents = PhotoPathsWithReasonAdapter_.getInstance_(getContext()).withPhotoPaths(order.getOrderDocuments());
		PhotoPathsWithReasonAdapter photos = PhotoPathsWithReasonAdapter_.getInstance_(getContext()).withPhotoPaths(order.getPhotos());

		LinearLayoutManager horizontalManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		LinearLayoutManager horizontalManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

		orderDocumentsView.setLayoutManager(horizontalManager);
		orderPhotosView.setLayoutManager(horizontalManager2);

		DividerItemDecoration decor = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
		Drawable verticalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.vertical_divider);
		decor.setDrawable(verticalDivider);

		orderDocumentsView.addItemDecoration(decor);
		orderPhotosView.addItemDecoration(decor);

		orderDocumentsView.setAdapter(documents);
		orderPhotosView.setAdapter(photos);
	}
}
