package net.aineuron.eagps.event.network.car;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 01.09.2017.
 */

public class CarStatusChangedEvent {
    public final Long carId;

    public CarStatusChangedEvent(Long carId) {
        this.carId = carId;
    }
}
