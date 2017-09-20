package net.aineuron.eagps.model.database.order;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Vit Veres on 31-May-17
 * as a part of Android-EAGPS project.
 */

public class Address extends RealmObject implements Serializable {
	private AddressDetail address;
	private Location location;

	public AddressDetail getAddress() {
		return address;
	}

	public void setAddress(AddressDetail address) {
		this.address = address;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}
