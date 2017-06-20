package net.aineuron.eagps.model.database;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by Vit Veres on 19-Jun-17
 * as a part of Android-EAGPS project.
 */

public class Message extends RealmObject {
	private Long id;
	private Date date;
	private String message;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
