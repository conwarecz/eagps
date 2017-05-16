package net.aineuron.eagps.model;

import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.model.database.Car;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vit Veres on 16-May-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class CarsManager {

	@Pref
	Pref_ pref;

	public List<Car> getAvailableCars() {
		List<Car> cars = new ArrayList<>();

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

		return cars;
	}

	public long getSelectedCarId() {
		return pref.selectedCar().get();
	}

	public void setSelectedCarId(long selectedCarId) {
		pref.edit().selectedCar().put(selectedCarId).apply();
	}

}