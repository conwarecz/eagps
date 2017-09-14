package net.aineuron.eagps.model.transfer;

/**
 * Created by Vit Veres on 16.08.2017
 * as a part of eagps project.
 */

public class KnownError {
	private int code;
	private String message;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
