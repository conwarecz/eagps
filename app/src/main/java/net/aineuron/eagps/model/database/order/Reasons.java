package net.aineuron.eagps.model.database.order;

import io.realm.RealmObject;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 27.10.2017.
 */

public class Reasons extends RealmObject {
    private String reasonForNoDocuments;
    private String reasonForNoPhotos;

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
