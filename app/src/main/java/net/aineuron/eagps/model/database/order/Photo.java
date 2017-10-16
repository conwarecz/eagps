package net.aineuron.eagps.model.database.order;

import io.realm.RealmObject;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 16.10.2017.
 */

public class Photo extends RealmObject {
    private Long id;
    private int type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
