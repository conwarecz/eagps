package net.aineuron.eagps.model.database;

import io.realm.RealmObject;

/**
 * Created by Vit Veres on 20-Jun-17
 * as a part of Android-EAGPS project.
 */

public class RealmString extends RealmObject {
	private String value;

	public RealmString() {
	}

	public RealmString(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
