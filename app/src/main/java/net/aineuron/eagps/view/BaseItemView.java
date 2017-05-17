package net.aineuron.eagps.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 * <p>
 * Type T is a view holder which needs to bind to the item view
 */
public abstract class BaseItemView<T> extends FrameLayout {
	protected T item;

	public BaseItemView(@NonNull Context context) {
		super(context);
	}

	public void bind(T item) {
		this.item = item;
		bindView();
	}

	protected abstract void bindView();

	public T getItem() {
		return item;
	}
}
