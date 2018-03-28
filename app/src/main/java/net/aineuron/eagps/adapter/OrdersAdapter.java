package net.aineuron.eagps.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.WindowManager;

import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.fragment.OrderAttachmentsFragment;
import net.aineuron.eagps.fragment.OrderDetailFragment;
import net.aineuron.eagps.fragment.TowFragment;
import net.aineuron.eagps.model.UserManager_;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.transfer.KnownError;
import net.aineuron.eagps.util.NetworkUtil;
import net.aineuron.eagps.view.ItemViewWrapper;
import net.aineuron.eagps.view.order.OrderItemView;
import net.aineuron.eagps.view.order.OrderItemView_;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY_ORDER;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_ASSIGNED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_ENTITY_FINISHED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_FINISHED;

/**
 * Created by Vit Veres on 20-Jun-17
 * as a part of Android-EAGPS project.
 */

public class OrdersAdapter extends RealmRecyclerViewAdapter<Order, ItemViewWrapper<OrderItemView>> {

	private static int width = 0;
	private MainActivityBase mMainActivityBase;

	public OrdersAdapter(@Nullable OrderedRealmCollection<Order> data) {
		super(data, true);
	}

	public void setMainActivityBase(MainActivityBase mainActivityBase) {
		mMainActivityBase = mainActivityBase;
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

		holder.getView().setOnClickListener(view -> {
			if (!NetworkUtil.isConnected(mMainActivityBase.getApplicationContext())) {
				KnownError knownError = new KnownError();
				knownError.setCode(400);
				knownError.setMessage("Nejste připojen k internetu");
				ClientProvider.postKnownError(knownError);
				return;
			}
			if (obj != null) {
				switch (obj.getStatus()) {
					case ORDER_STATE_ASSIGNED:
						UserManager_.getInstance_(holder.getView().getContext()).setSelectedStateId(STATE_ID_BUSY_ORDER);
						mMainActivityBase.showFragment(TowFragment.newInstance(obj.getId()));
						break;
					case ORDER_STATE_ENTITY_FINISHED:
						mMainActivityBase.showFragment(TowFragment.newInstance(obj.getId()));
						break;
					case ORDER_STATE_FINISHED:
						mMainActivityBase.showFragment(OrderAttachmentsFragment.newInstance(obj.getId()));
						break;
					default:
						mMainActivityBase.showFragment(OrderDetailFragment.newInstance(obj.getId(), null));
						break;
				}
			}
		});

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
