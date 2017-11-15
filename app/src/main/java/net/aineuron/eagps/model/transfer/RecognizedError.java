package net.aineuron.eagps.model.transfer;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.aineuron.eagps.adapter.RealmStringListTypeAdapter;
import net.aineuron.eagps.model.database.RealmString;

import io.realm.RealmList;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 13.11.2017.
 */

public class RecognizedError {
    private Long Code;
    private String Message;

    public static RecognizedError getError(String json) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:sss")
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {
                        }.getType(),
                        RealmStringListTypeAdapter.INSTANCE)
                .create();
        RecognizedError error = gson.fromJson(json, RecognizedError.class);
        return error;
    }

    public Long getCode() {
        return Code;
    }

    public void setCode(Long code) {
        Code = code;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
