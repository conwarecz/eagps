package net.aineuron.eagps.view.dispatcherselectcar;

import android.content.Context;
import android.support.annotation.NonNull;

import net.aineuron.eagps.adapter.DispatcherSelectCarAdapter;
import net.aineuron.eagps.model.viewmodel.WorkerSelectCarViewModel;
import net.aineuron.eagps.view.BaseItemView;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 31.08.2017.
 */

public abstract class BaseDispatcherSelectCarItemView extends BaseItemView<WorkerSelectCarViewModel> {
    protected DispatcherSelectCarAdapter.OnItemChange onItemChange;

    public BaseDispatcherSelectCarItemView(@NonNull Context context) {
        super(context);
    }

    public void setOnChangeListener(DispatcherSelectCarAdapter.OnItemChange onItemChange) {
        this.onItemChange = onItemChange;
    }
}