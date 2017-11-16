package net.aineuron.eagps.event.network.order;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 15.11.2017.
 */

public class OrderAcceptedEvent {
    public final Long orderId;

    public OrderAcceptedEvent(Long orderId) {
        this.orderId = orderId;
    }
}
