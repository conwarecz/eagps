package net.aineuron.eagps.view.workerselectcar;

import android.content.Context;
import android.support.annotation.NonNull;

import net.aineuron.eagps.adapter.WorkerSelectCarAdapter;
import net.aineuron.eagps.model.viewmodel.WorkerSelectCarViewModel;
import net.aineuron.eagps.view.BaseItemView;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 */

public abstract class BaseWorkerSelectCarItemView extends BaseItemView<WorkerSelectCarViewModel> {
	protected WorkerSelectCarAdapter.OnItemChange onItemChange;

	public BaseWorkerSelectCarItemView(@NonNull Context context) {
		super(context);
	}

	public void setOnChangeListener(WorkerSelectCarAdapter.OnItemChange onItemChange) {
		this.onItemChange = onItemChange;
	}
}
