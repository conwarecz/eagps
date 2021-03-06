package net.aineuron.eagps.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.viewmodel.WorkerSelectCarViewModel;
import net.aineuron.eagps.view.ItemViewWrapper;
import net.aineuron.eagps.view.workerselectcar.BaseWorkerSelectCarItemView;
import net.aineuron.eagps.view.workerselectcar.WorkerSelectCarItemView_;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 */

@EBean
public class WorkerSelectCarAdapter extends BaseRecyclerViewAdapter<WorkerSelectCarViewModel, BaseWorkerSelectCarItemView> {

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
	protected BaseWorkerSelectCarItemView onCreateItemView(ViewGroup parent, int viewType) {
		return WorkerSelectCarItemView_.build(context);
	}

	@Override
	public void onBindViewHolder(ItemViewWrapper<BaseWorkerSelectCarItemView> holder, int position) {
		WorkerSelectCarViewModel item = items.get(position);
		BaseWorkerSelectCarItemView view = holder.getView();
		view.bind(item);
		view.setOnChangeListener(this::notifyDataChanged);
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
