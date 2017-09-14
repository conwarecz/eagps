package net.aineuron.eagps.event.network.car;

import net.aineuron.eagps.model.database.Car;

import java.util.List;

/**
 * Created by Vit Veres on 16-May-17
 * as a part of Android-EAGPS project.
 */

public class CarsDownloadedEvent {
	public final List<Car> cars;

	public CarsDownloadedEvent(List<Car> cars) {
		this.cars = cars;
	}
}
