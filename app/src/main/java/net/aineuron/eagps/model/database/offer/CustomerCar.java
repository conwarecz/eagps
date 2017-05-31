package net.aineuron.eagps.model.database.offer;

/**
 * Created by Vit Veres on 31-May-17
 * as a part of Android-EAGPS project.
 */

public class CustomerCar {
	private String car;
	private String weight;
	private String licensePlate;

	public String getCar() {
		return car;
	}

	public void setCar(String car) {
		this.car = car;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}
}
