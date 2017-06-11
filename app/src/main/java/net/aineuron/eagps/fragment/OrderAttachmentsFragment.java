package net.aineuron.eagps.fragment;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
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
	}
}
