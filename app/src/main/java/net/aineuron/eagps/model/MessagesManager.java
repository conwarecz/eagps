package net.aineuron.eagps.model;

import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.event.network.MessageStatusChangedEvent;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;

import io.realm.Realm;

/**
 * Created by Vit Veres on 14.08.2017
 * as a part of eagps project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class MessagesManager {

	@EventBusGreenRobot
	EventBus eventBus;

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

	public boolean checkUnreadMessage() {
		Realm db = RealmHelper.getDb();
		boolean unread = false;
		Message message = db.where(Message.class).equalTo("read", false).findFirst();
		if (message != null) {
			unread = true;
		}
		eventBus.post(new MessageStatusChangedEvent(unread));
		return unread;
	}
}
