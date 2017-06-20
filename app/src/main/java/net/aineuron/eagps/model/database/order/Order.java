package net.aineuron.eagps.model.database.order;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by Vit Veres on 06-Jun-17
 * as a part of Android-EAGPS project.
 */

public class Order extends RealmObject {
	private Long id;
	private String claimNumber;
	private Date time;
	private String clientName;
	private String clientPhone;
	private ClientCar car;
	private Address clientAddress;
	private DestinationAddress destinationAddress;
	private String eventDescription;
	private Limitation limitation;
	private PhotoPathsWithReason orderDocuments = new PhotoPathsWithReason();
	private PhotoPathsWithReason photos = new PhotoPathsWithReason();
	private boolean isSent;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClaimNumber() {
		return claimNumber;
	}

	public void setClaimNumber(String claimNumber) {
		this.claimNumber = claimNumber;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientPhone() {
		return clientPhone;
	}

	public void setClientPhone(String clientPhone) {
		this.clientPhone = clientPhone;
	}

	public ClientCar getCar() {
		return car;
	}

	public void setCar(ClientCar car) {
		this.car = car;
	}

	public Address getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(Address clientAddress) {
		this.clientAddress = clientAddress;
	}

	public DestinationAddress getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(DestinationAddress destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getEventDescription() {
		return eventDescription;
	}

	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}

	public Limitation getLimitation() {
		return limitation;
	}

	public void setLimitation(Limitation limitation) {
		this.limitation = limitation;
	}

	public PhotoPathsWithReason getOrderDocuments() {
		return orderDocuments;
	}

	public void setOrderDocuments(PhotoPathsWithReason orderDocuments) {
		this.orderDocuments = orderDocuments;
	}

	public PhotoPathsWithReason getPhotos() {
		return photos;
	}

	public void setPhotos(PhotoPathsWithReason photos) {
		this.photos = photos;
	}

	public boolean isSent() {
		return isSent;
	}

	public void setSent(boolean sent) {
		isSent = sent;
	}
}
