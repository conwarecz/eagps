package net.aineuron.eagps.model.database.order;

import com.google.gson.annotations.SerializedName;

import net.aineuron.eagps.model.database.RealmString;

import java.util.Date;

import io.realm.RealmList;

/**
 * Created by Vit Veres on 31-May-17
 * as a part of Android-EAGPS project.
 */

public class Offer {
	private Long id;
	private Date timeCreated;
	private String clientFirstName;
	private String clientLastName;
	private String clientPhone;
	private String clientCarModel;
	private String clientCarWeight;
	private String clientLicencePlate;
	@SerializedName("location")
	private Address clientAddress;
	private DestinationAddress destinationAddress;
	private RealmList<RealmString> eventDescription;
	private Limitation limitation;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}

	public String getClientFirstName() {
		return clientFirstName;
	}

	public void setClientFirstName(String clientFirstName) {
		this.clientFirstName = clientFirstName;
	}

	public String getClientLastName() {
		return clientLastName;
	}

	public void setClientLastName(String clientLastName) {
		this.clientLastName = clientLastName;
	}

	public String getClientPhone() {
		return clientPhone;
	}

	public void setClientPhone(String clientPhone) {
		this.clientPhone = clientPhone;
	}

	public String getClientCarModel() {
		return clientCarModel;
	}

	public void setClientCarModel(String clientCarModel) {
		this.clientCarModel = clientCarModel;
	}

	public String getClientCarWeight() {
		return clientCarWeight;
	}

	public void setClientCarWeight(String clientCarWeight) {
		this.clientCarWeight = clientCarWeight;
	}

	public String getClientLicencePlate() {
		return clientLicencePlate;
	}

	public void setClientLicencePlate(String clientLicencePlate) {
		this.clientLicencePlate = clientLicencePlate;
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

	public RealmList<RealmString> getEventDescription() {
		return eventDescription;
	}

	public void setEventDescription(RealmList<RealmString> eventDescription) {
		this.eventDescription = eventDescription;
	}

	public Limitation getLimitation() {
		return limitation;
	}

	public void setLimitation(Limitation limitation) {
		this.limitation = limitation;
	}
}
