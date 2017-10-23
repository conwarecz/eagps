package net.aineuron.eagps.model.transfer.tender;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 23.10.2017.
 */

public class TenderAcceptModel {
    private Long entityId;
    private String smsNumber;
    private String userName;
    private Long departureDelayMinutes;

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getDepartureDelayMinutes() {
        return departureDelayMinutes;
    }

    public void setDepartureDelayMinutes(Long departureDelayMinutes) {
        this.departureDelayMinutes = departureDelayMinutes;
    }
}
