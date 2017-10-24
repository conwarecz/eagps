package net.aineuron.eagps.model.database.order;

import io.realm.RealmObject;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 16.10.2017.
 */

public class Photo extends RealmObject {
    public final static int PHOTO_TYPE_PHOTO = 1;
    public final static int PHOTO_TYPE_DOCUMENT = 2;

    private Long id;
    private int type;
    private String path;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
