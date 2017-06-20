package net.aineuron.eagps.event.ui;

/**
 * Created by Vit Veres on 20-Jun-17
 * as a part of Android-EAGPS project.
 */

public class MessageClickedEvent {
	public final Long messageId;

	public MessageClickedEvent(Long messageId) {
		this.messageId = messageId;
	}
}
