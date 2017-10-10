package net.aineuron.eagps.model.transfer.tender;

import java.io.Serializable;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 09.10.2017.
 */

public class AddressDetailSerializable implements Serializable {
    private String street;
    private String city;
    private String zipCode;
    private String country;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
