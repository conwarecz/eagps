package net.aineuron.eagps.model.transfer.tender;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 09.10.2017.
 */

public class OrderSerializable implements Serializable {
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
    private AddressSerializable clientAddress;
    private List<String> eventDescription;
    private LimitationSerializable limitation;
    private String workshopName;
    private int destinationType;
    private AddressSerializable destinationAddress;

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

    public AddressSerializable getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(AddressSerializable clientAddress) {
        this.clientAddress = clientAddress;
    }

    public List<String> getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(List<String> eventDescription) {
        this.eventDescription = eventDescription;
    }

    public LimitationSerializable getLimitation() {
        return limitation;
    }

    public void setLimitation(LimitationSerializable limitation) {
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

    public AddressSerializable getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(AddressSerializable destinationAddress) {
        this.destinationAddress = destinationAddress;
    }
}
