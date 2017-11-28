package net.aineuron.eagps.event.network.car;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 31.10.2017.
 */

public class CarReleasedEvent {
    public final Long newSelectedCarId;

    public CarReleasedEvent(Long newSelectedCarId) {
        this.newSelectedCarId = newSelectedCarId;
    }
}
