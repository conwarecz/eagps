package net.aineuron.eagps.model.database.order;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import net.aineuron.eagps.adapter.RealmStringListTypeAdapter;
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
	public static final int ORDER_STATE_CREATED = 1;
	public static final int ORDER_STATE_ASSIGNED = 2;
	public static final int ORDER_STATE_ENTITY_FINISHED = 3;
	public static final int ORDER_STATE_FINISHED = 4;
	public static final int ORDER_STATE_SENT = 5;
	public static final int ORDER_STATE_CANCELLED = 6;

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
	@SerializedName("Location")
	private Address clientAddress;
	private RealmList<RealmString> eventDescription;
	private Limitation limitation;
	private RealmList<Photo> photos = new RealmList<>();
	private Date estimatedDepartureTime;

	private String workshopName;
	private int destinationType;
	@SerializedName("Destination")
	private Address destinationAddress;

	private String entityName;

	private String reasonForNoDocuments;
	private String reasonForNoPhotos;
	private Boolean photosProvided;
	private Boolean orderSheetProvided;

	private boolean isSent;

	public static Order getFromJson(String json) {
		Gson gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd'T'HH:mm:sss")
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				.registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {
						}.getType(),
						RealmStringListTypeAdapter.INSTANCE)
				.create();
		return gson.fromJson(json, Order.class);
	}

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

	public Address getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(Address destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public boolean isSent() {
		return isSent;
	}

	public void setSent(boolean sent) {
		isSent = sent;
	}

	public RealmList<Photo> getPhotos() {
		return photos;
	}

	public void setPhotos(RealmList<Photo> photos) {
		this.photos = photos;
	}

	public String getReasonForNoDocuments() {
		return reasonForNoDocuments;
	}

	public void setReasonForNoDocuments(String reasonForNoDocuments) {
		this.reasonForNoDocuments = reasonForNoDocuments;
	}

	public String getReasonForNoPhotos() {
		return reasonForNoPhotos;
	}

	public void setReasonForNoPhotos(String reasonForNoPhotos) {
		this.reasonForNoPhotos = reasonForNoPhotos;
	}

	public Date getEstimatedDepartureTime() {
		return estimatedDepartureTime;
	}

	public void setEstimatedDepartureTime(Date estimatedDepartureTime) {
		this.estimatedDepartureTime = estimatedDepartureTime;
	}

	public Boolean getPhotosProvided() {
		return photosProvided;
	}

	public void setPhotosProvided(Boolean photosProvided) {
		this.photosProvided = photosProvided;
	}

	public Boolean getOrderSheetProvided() {
		return orderSheetProvided;
	}

	public void setOrderSheetProvided(Boolean orderSheetProvided) {
		this.orderSheetProvided = orderSheetProvided;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
}

