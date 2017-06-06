package net.aineuron.eagps.model.database.order;

import java.util.Date;

/**
 * Created by Vit Veres on 31-May-17
 * as a part of Android-EAGPS project.
 */

public class Offer {
	private Long id;
	private Date time;
	private String clientName;
	private String clientPhone;
	private ClientCar car;
	private Address clientAddress;
	private DestinationAddress destinationAddress;
	private String eventDescription;
	private Limitation limitation;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
}
