package net.aineuron.eagps.model.database;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 24.11.2017.
 */

public class UserWhoKickedMeFromCar extends RealmObject implements Serializable {
    private Long id;
    private String username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
