package net.aineuron.eagps.event.network.car;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 30.10.2017.
 */

public class DispatcherRefreshCarsEvent {
    public final Long entityId;
    public final Long entityState;

    public DispatcherRefreshCarsEvent(Long entityId, Long entityState) {
        this.entityId = entityId;
        this.entityState = entityState;
    }
}
