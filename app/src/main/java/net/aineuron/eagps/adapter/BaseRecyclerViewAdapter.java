package net.aineuron.eagps.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import net.aineuron.eagps.view.ItemViewWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vit Veres on 2/15/2017
 * as a part of Android-AkcniCeny project.
 */

public abstract class BaseRecyclerViewAdapter<T, V extends View> extends RecyclerView.Adapter<ItemViewWrapper<V>> {
	protected List<T> items = new ArrayList<T>();

	@Override
	public int getItemCount() {
		return items.size();
	}

	@Override
	public final ItemViewWrapper<V> onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ItemViewWrapper<V>(onCreateItemView(parent, viewType));
	}

	protected abstract V onCreateItemView(ViewGroup parent, int viewType);

	// additional methods to manipulate the items
}
