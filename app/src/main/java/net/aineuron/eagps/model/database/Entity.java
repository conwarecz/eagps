package net.aineuron.eagps.model.database;

import java.io.Serializable;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 26.09.2017.
 */

public class Entity implements Serializable {
    private Long entityId;
    private Long entityStatus;
    private String licencePlate;
    private String name;
    private String userName;

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(Long entityStatus) {
        this.entityStatus = entityStatus;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public void setLicencePlate(String licencePlate) {
        this.licencePlate = licencePlate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
