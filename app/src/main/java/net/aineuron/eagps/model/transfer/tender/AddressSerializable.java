package net.aineuron.eagps.model.transfer.tender;

import java.io.Serializable;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 09.10.2017.
 */

public class AddressSerializable implements Serializable {
    private AddressDetailSerializable address;
    private LocationSerializable location;

    public AddressDetailSerializable getAddress() {
        return address;
    }

    public void setAddress(AddressDetailSerializable address) {
        this.address = address;
    }

    public LocationSerializable getLocation() {
        return location;
    }

    public void setLocation(LocationSerializable location) {
        this.location = location;
    }
}
