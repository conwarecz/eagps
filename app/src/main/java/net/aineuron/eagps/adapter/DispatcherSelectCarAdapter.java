package net.aineuron.eagps.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.WindowManager;

import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.viewmodel.WorkerSelectCarViewModel;
import net.aineuron.eagps.view.ItemViewWrapper;
import net.aineuron.eagps.view.dispatcherselectcar.DispatcherSelectCarItemView;
import net.aineuron.eagps.view.dispatcherselectcar.DispatcherSelectCarItemView_;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 31.08.2017.
 */

@EBean
public class DispatcherSelectCarAdapter extends BaseRecyclerViewAdapter<WorkerSelectCarViewModel, DispatcherSelectCarItemView> {
    private static int width = 0;
    @RootContext
    Context context;
    @Bean
    UserManager userManager;
    private List<Car> cars = new ArrayList<>();

    public void setCars(List<Car> cars) {
        this.cars = cars;
        notifyDataChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        //notifyDataChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    protected DispatcherSelectCarItemView onCreateItemView(ViewGroup parent, int viewType) {
        return DispatcherSelectCarItemView_.build(context);
    }

    @Override
    public void onBindViewHolder(ItemViewWrapper<DispatcherSelectCarItemView> holder, int position) {
        WorkerSelectCarViewModel item = items.get(position);
        DispatcherSelectCarItemView view = holder.getView();
        view.bind(item.getCar());

        if (width <= 0) {
            WindowManager windowManager = (WindowManager) holder.getView().getContext().getSystemService(Context.WINDOW_SERVICE);
            width = windowManager.getDefaultDisplay().getWidth();
        }
        holder.getView().setLayoutParams(new RecyclerView.LayoutParams(width, RecyclerView.LayoutParams.WRAP_CONTENT));
    }

    public void notifyDataChanged() {
        items = new ArrayList<>();
        Long selectedCarId = userManager.getSelectedCarId();

        for (Car car : cars) {
            WorkerSelectCarViewModel carViewModel = new WorkerSelectCarViewModel();
            carViewModel.withCar(car);
            if (selectedCarId != null && selectedCarId.equals(car.getId())) {
                carViewModel.isSelected(true);
            }
            items.add(carViewModel);
        }

        notifyDataSetChanged();
    }

    public interface OnItemChange {
        void onChange();
    }
}