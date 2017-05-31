package net.aineuron.eagps.model.database.offer;

/**
 * Created by Vit Veres on 31-May-17
 * as a part of Android-EAGPS project.
 */

public class Address {
	private String street;
	private String city;
	private String zipCode;
	private String lat;
	private String lon;

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

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}
}
