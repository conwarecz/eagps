package net.aineuron.eagps.model;

import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.EBean;

import io.realm.Realm;

/**
 * Created by Vit Veres on 14.08.2017
 * as a part of eagps project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class MessagesManager {

	public void setMessageRead(Long id, boolean isRead) {
		Realm db = RealmHelper.getDb();

		db.executeTransaction(realm -> {
			Message message = realm.where(Message.class).equalTo("id", id).findFirst();
			if (message != null) {
				message.setRead(isRead);
			}
		});

		db.close();
	}
}
