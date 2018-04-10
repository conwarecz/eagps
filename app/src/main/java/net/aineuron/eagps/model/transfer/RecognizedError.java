package net.aineuron.eagps.model.transfer;

import net.aineuron.eagps.client.ClientProvider;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 13.11.2017.
 */

public class RecognizedError {
	private Long Code = -1L;
	private String Message = "";

    public static RecognizedError getError(String json) {
	    RecognizedError error = ClientProvider.gson.fromJson(json, RecognizedError.class);
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
