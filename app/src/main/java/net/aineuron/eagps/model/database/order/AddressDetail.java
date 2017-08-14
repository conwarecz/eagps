package net.aineuron.eagps.model.database.order;

import io.realm.RealmObject;

/**
 * Created by Vit Veres on 14.08.2017
 * as a part of eagps project.
 */

public class AddressDetail extends RealmObject {
	private String street;
	private String city;
	private String zipCode;
	private String country;

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
