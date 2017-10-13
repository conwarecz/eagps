package net.aineuron.eagps.event.network;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 12.10.2017.
 */

public class MessageStatusChangedEvent {
    public final boolean unread;

    public MessageStatusChangedEvent(boolean unread) {
        this.unread = unread;
    }
}
