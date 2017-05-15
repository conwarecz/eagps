package net.aineuron.eagps.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Vit Veres on 2/15/2017
 * as a part of Android-AkcniCeny project.
 */

public class ItemViewWrapper<V extends View> extends RecyclerView.ViewHolder {

	private V view;

	public ItemViewWrapper(V itemView) {
		super(itemView);
		view = itemView;
	}

	public V getView() {
		return view;
	}
}