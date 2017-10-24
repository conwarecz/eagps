package net.aineuron.eagps.event.network.car;

/**
 * Created by Vit Veres on 22-May-17
 * as a part of Android-EAGPS project.
 */

public class StateSelectedEvent {
    public final Long state;

    public StateSelectedEvent(Long state) {
        this.state = state;
    }
}
