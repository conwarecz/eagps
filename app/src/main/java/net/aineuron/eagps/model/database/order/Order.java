package net.aineuron.eagps.model.database.order;

import com.google.gson.annotations.SerializedName;

import net.aineuron.eagps.model.database.RealmString;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Vit Veres on 06-Jun-17
 * as a part of Android-EAGPS project.
 */

public class Order extends RealmObject {
	@PrimaryKey
	private Long id;
	private String claimSaxCode;
	private Date timeCreated;
	private int serviceType;
	private int status;
	private String clientFirstName;
	private String clientLastName;
	private String clientPhone;
	private String clientCarModel;
	private String clientCarWeight;
	private String clientCarLicencePlate;
	@SerializedName("location")
	private Address clientAddress;
	private RealmList<RealmString> eventDescription;
	private Limitation limitation;
	private PhotoPathsWithReason orderDocuments = new PhotoPathsWithReason();
	private PhotoPathsWithReason photos = new PhotoPathsWithReason();

	private String workshopName;
	private int destinationType;
	@SerializedName("destination")
	private DestinationAddress destinationAddress;

	private boolean orderSheetProvided;
	private boolean photosProvided;

	private boolean isSent;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClaimSaxCode() {
		return claimSaxCode;
	}

	public void setClaimSaxCode(String claimSaxCode) {
		this.claimSaxCode = claimSaxCode;
	}

	public Date getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}

	public int getServiceType() {
		return serviceType;
	}

	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public String getClientCarLicencePlate() {
		return clientCarLicencePlate;
	}

	public void setClientCarLicencePlate(String clientCarLicencePlate) {
		this.clientCarLicencePlate = clientCarLicencePlate;
	}

	public Address getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(Address clientAddress) {
		this.clientAddress = clientAddress;
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

	public String getWorkshopName() {
		return workshopName;
	}

	public void setWorkshopName(String workshopName) {
		this.workshopName = workshopName;
	}

	public int getDestinationType() {
		return destinationType;
	}

	public void setDestinationType(int destinationType) {
		this.destinationType = destinationType;
	}

	public DestinationAddress getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(DestinationAddress destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public boolean isOrderSheetProvided() {
		return orderSheetProvided;
	}

	public void setOrderSheetProvided(boolean orderSheetProvided) {
		this.orderSheetProvided = orderSheetProvided;
	}

	public boolean isPhotosProvided() {
		return photosProvided;
	}

	public void setPhotosProvided(boolean photosProvided) {
		this.photosProvided = photosProvided;
	}

	public boolean isSent() {
		return isSent;
	}

	public void setSent(boolean sent) {
		isSent = sent;
	}
}
