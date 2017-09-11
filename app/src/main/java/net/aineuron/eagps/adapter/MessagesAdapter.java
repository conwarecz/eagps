package net.aineuron.eagps.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.WindowManager;

import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.view.ItemViewWrapper;
import net.aineuron.eagps.view.message.MessageItemView;
import net.aineuron.eagps.view.message.MessageItemView_;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Vit Veres on 19-Jun-17
 * as a part of Android-EAGPS project.
 */

public class MessagesAdapter extends RealmRecyclerViewAdapter<Message, ItemViewWrapper<MessageItemView>> {

	private static int width = 0;

	public MessagesAdapter(OrderedRealmCollection<Message> data) {
		super(data, true);
	}

	@Override
	public ItemViewWrapper<MessageItemView> onCreateViewHolder(ViewGroup parent, int viewType) {
		MessageItemView itemView = MessageItemView_.build(parent.getContext());
		return new ItemViewWrapper<>(itemView);
	}

	@Override
	public void onBindViewHolder(ItemViewWrapper<MessageItemView> holder, int position) {
		final Message obj = getItem(position);
		holder.getView().bind(obj);

		if (width <= 0) {
			WindowManager windowManager = (WindowManager) holder.getView().getContext().getSystemService(Context.WINDOW_SERVICE);
			width = windowManager.getDefaultDisplay().getWidth();
		}

		holder.getView().setLayoutParams(new RecyclerView.LayoutParams(width, RecyclerView.LayoutParams.WRAP_CONTENT));
	}

	@Override
	public long getItemId(int index) {
		//noinspection ConstantConditions
		return getItem(index).getId();
	}
}
