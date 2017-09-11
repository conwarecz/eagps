package net.aineuron.eagps.event.network.order;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 07.09.2017.
 */

public class OrderFinalizedEvent {
    public final Long orderId;

    public OrderFinalizedEvent(Long orderId) {
        this.orderId = orderId;
    }
}
