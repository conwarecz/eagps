package net.aineuron.eagps.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.WindowManager;

import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.view.ItemViewWrapper;
import net.aineuron.eagps.view.order.OrderItemView;
import net.aineuron.eagps.view.order.OrderItemView_;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Vit Veres on 20-Jun-17
 * as a part of Android-EAGPS project.
 */

public class OrdersAdapter extends RealmRecyclerViewAdapter<Order, ItemViewWrapper<OrderItemView>> {

	private static int width = 0;

	public OrdersAdapter(@Nullable OrderedRealmCollection<Order> data) {
		super(data, true);
	}

	@Override
	public ItemViewWrapper<OrderItemView> onCreateViewHolder(ViewGroup viewGroup, int i) {
		OrderItemView itemView = OrderItemView_.build(viewGroup.getContext());
		return new ItemViewWrapper<>(itemView);
	}

	@Override
	public void onBindViewHolder(ItemViewWrapper<OrderItemView> holder, int position) {
		final Order obj = getItem(position);
		holder.getView().bind(obj);

		if (width <= 0) {
			WindowManager windowManager = (WindowManager) holder.getView().getContext().getSystemService(Context.WINDOW_SERVICE);
			width = windowManager.getDefaultDisplay().getWidth();
		}
		holder.getView().setLayoutParams(new RecyclerView.LayoutParams(width, RecyclerView.LayoutParams.WRAP_CONTENT));
	}

	@Override
	public void updateData(@Nullable OrderedRealmCollection<Order> data) {
		super.updateData(data);
	}
}
