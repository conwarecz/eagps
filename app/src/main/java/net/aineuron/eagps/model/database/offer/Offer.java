package net.aineuron.eagps.model.database.offer;

import java.util.Date;

/**
 * Created by Vit Veres on 31-May-17
 * as a part of Android-EAGPS project.
 */

public class Offer {
	private Long offerId;
	private String name;
	private String phone;
	private CustomerCar car;
	private Address customerAddress;
	private Address destinationAddress;
	private String description;
	private Limitation limitation;
	private Date offerDate;
	private Date offerExpirationDate;

	public Long getOfferId() {
		return offerId;
	}

	public void setOfferId(Long offerId) {
		this.offerId = offerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public CustomerCar getCar() {
		return car;
	}

	public void setCar(CustomerCar car) {
		this.car = car;
	}

	public Address getCustomerAddress() {
		return customerAddress;
	}

	public void setCustomerAddress(Address customerAddress) {
		this.customerAddress = customerAddress;
	}

	public Address getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(Address destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Limitation getLimitation() {
		return limitation;
	}

	public void setLimitation(Limitation limitation) {
		this.limitation = limitation;
	}

	public Date getOfferDate() {
		return offerDate;
	}

	public void setOfferDate(Date offerDate) {
		this.offerDate = offerDate;
	}

	public Date getOfferExpirationDate() {
		return offerExpirationDate;
	}

	public void setOfferExpirationDate(Date offerExpirationDate) {
		this.offerExpirationDate = offerExpirationDate;
	}
}
