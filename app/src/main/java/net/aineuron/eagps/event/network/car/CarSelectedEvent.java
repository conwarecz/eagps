package net.aineuron.eagps.event.network.car;

/**
 * Created by Vit Veres on 16-May-17
 * as a part of Android-EAGPS project.
 */

public class CarSelectedEvent {
    public final Long carStateId;

    public CarSelectedEvent(Long carStateId) {
        this.carStateId = carStateId;
    }
}
