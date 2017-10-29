package net.aineuron.eagps.model.database.order;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 26.10.2017.
 */

public class LocalReasons extends RealmObject {
    @PrimaryKey
    private Long orderId;
    private Reasons reasons;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Reasons getReasons() {
        return reasons;
    }

    public void setReasons(Reasons reasons) {
        this.reasons = reasons;
    }
}
