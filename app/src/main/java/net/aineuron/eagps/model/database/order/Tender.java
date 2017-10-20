package net.aineuron.eagps.model.database.order;

import com.google.gson.FieldNamingPolicy;
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
    private Order Order;
    private Message Message;
    private Long TenderId;
    private boolean isMessage = false;

    public static Order getOrderFromJson(String json) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:sss")
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
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
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {
                        }.getType(),
                        RealmStringListTypeAdapter.INSTANCE)
                .create();
        Tender tender = gson.fromJson(json, Tender.class);
        return tender.getMessage();
    }

    public static Tender getTender(String json) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:sss")
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {
                        }.getType(),
                        RealmStringListTypeAdapter.INSTANCE)
                .create();
        return gson.fromJson(json, Tender.class);
    }

    public net.aineuron.eagps.model.database.order.Order getOrder() {
        return Order;
    }

    public void setOrder(net.aineuron.eagps.model.database.order.Order order) {
        Order = order;
    }

    public net.aineuron.eagps.model.database.Message getMessage() {
        return Message;
    }

    public void setMessage(net.aineuron.eagps.model.database.Message message) {
        Message = message;
    }

    public Long getTenderId() {
        return TenderId;
    }

    public void setTenderId(Long tenderId) {
        TenderId = tenderId;
    }
}
