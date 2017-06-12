package net.aineuron.eagps.model;

import net.aineuron.eagps.model.database.Car;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vit Veres on 16-May-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class CarsManager {

	private List<Car> cars;

	public List<Car> getAvailableCars() {
		cars = new ArrayList<>();

		// TODO: Get real cars :)
		// Mock
		for (long i = 0; i < 20; i++) {
			Car car = new Car();
			car.setId(i);
			car.setStatusId(0L);
			car.setUserId(0L);
			car.setLicensePlate(String.format("1AB %04d", i));
			car.setModel("Peugeot Expert");
			cars.add(car);
		}

		return cars;
	}

	public Car getCarById(Long carId) {
		for (Car car : cars) {
			if (car.getId().equals(carId)) {
				return car;
			}
		}

		return null;
	}
}
