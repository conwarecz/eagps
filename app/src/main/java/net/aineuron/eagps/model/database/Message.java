package net.aineuron.eagps.model.database;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Vit Veres on 19-Jun-17
 * as a part of Android-EAGPS project.
 */

public class Message extends RealmObject {
	@PrimaryKey
	private Long id;
	private Date time;
	private String text;
	private boolean read;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}
}
