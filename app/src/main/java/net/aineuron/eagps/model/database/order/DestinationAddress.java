package net.aineuron.eagps.model.database.order;

/**
 * Created by Vit Veres on 05-Jun-17
 * as a part of Android-EAGPS project.
 */

public class DestinationAddress {
	private String name;
	private Address address;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
}
