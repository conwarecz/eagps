package net.aineuron.eagps.model.database.order;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.aineuron.eagps.adapter.RealmStringListTypeAdapter;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.RealmString;

import java.io.Serializable;

import io.realm.RealmList;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 20.09.2017.
 */

public class Tender implements Serializable {
    private Order order;
    private Message message;
    private String tenderId;
    private boolean isMessage = false;

    public static Order getOrderFromJson(String json) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:sss")
//                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {
                        }.getType(),
                        RealmStringListTypeAdapter.INSTANCE)
                .create();
        Tender tender = gson.fromJson(json, Tender.class);
        return tender.getOrder();
    }

    public static Message getMessageFromJson(String json) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:sss")
//                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {
                        }.getType(),
                        RealmStringListTypeAdapter.INSTANCE)
                .create();
        Tender tender = gson.fromJson(json, Tender.class);
        return tender.getMessage();
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getTenderId() {
        return tenderId;
    }

    public void setTenderId(String tenderId) {
        this.tenderId = tenderId;
    }

    public Message getMessage() {
        return message;
    }

    public boolean isMessage() {
        isMessage = message != null;
        return isMessage;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
