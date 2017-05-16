package net.aineuron.eagps.model.viewmodel;

import net.aineuron.eagps.model.database.Car;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 */

public class WorkerSelectCarViewModel {
	private boolean isSelected;
	private Car car;

	public WorkerSelectCarViewModel() {
	}

	public WorkerSelectCarViewModel isSelected(boolean isSelected) {
		this.isSelected = isSelected;
		return this;
	}

	public WorkerSelectCarViewModel withCar(Car car) {
		this.car = car;
		return this;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public Car getCar() {
		return car;
	}
}
