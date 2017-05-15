package net.aineuron.eagps.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import net.aineuron.eagps.model.Car;
import net.aineuron.eagps.model.viewmodel.WorkerSelectCarViewModel;
import net.aineuron.eagps.view.ItemViewWrapper;
import net.aineuron.eagps.view.workerselectcar.BaseWorkerSelectCarItemView;
import net.aineuron.eagps.view.workerselectcar.WorkerSelectCarItemView_;

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

	private List<Car> cars = new ArrayList<>();

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		initCars();
		notifyDataChanged();
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
	}

	private void notifyDataChanged() {
		items = new ArrayList<>();

		for (Car car : cars) {
			WorkerSelectCarViewModel carViewModel = new WorkerSelectCarViewModel();
			carViewModel.withCar(car);
			items.add(carViewModel);
		}

		notifyDataSetChanged();
	}

	private void initCars() {
		cars = new ArrayList<>();

		// TODO: Get real cars :)
		// Mock
		for (int i = 0; i < 20; i++) {
			Car car = new Car();
			car.setId(i);
			car.setStatus(0);
			car.setUserId(0);
			car.setLicensePlate(String.format("1AB %04d", i));
			car.setModel("Peugeot Expert");
			cars.add(car);
		}
	}
}
