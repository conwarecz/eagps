package net.aineuron.eagps.model.database.order;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 26.10.2017.
 */

public class LocalReasons extends RealmObject {
    @PrimaryKey
    private Long orderId;
    private String reasonForNoDocuments;
    private String reasonForNoPhotos;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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
}
