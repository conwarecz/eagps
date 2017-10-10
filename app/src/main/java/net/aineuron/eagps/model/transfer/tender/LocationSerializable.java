package net.aineuron.eagps.model.transfer.tender;

import java.io.Serializable;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 09.10.2017.
 */

public class LocationSerializable implements Serializable {
    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
