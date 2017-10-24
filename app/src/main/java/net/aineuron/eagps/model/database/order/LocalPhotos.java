package net.aineuron.eagps.model.database.order;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 24.10.2017.
 */

public class LocalPhotos extends RealmObject {
    @PrimaryKey
    private Long orderId;
    private RealmList<Photo> localPhotos = new RealmList<>();

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public RealmList<Photo> getLocalPhotos() {
        return localPhotos;
    }

    public void setLocalPhotos(RealmList<Photo> localPhotos) {
        this.localPhotos = localPhotos;
    }
}
