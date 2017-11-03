package net.aineuron.eagps.event.network;

/**
 * Created by Vit Veres on 19.2.2016
 * as a part of AlTraceabilitySystem project.
 */
public class ApiErrorEvent {
	public final Throwable throwable;
    public final String message;

    public ApiErrorEvent(Throwable throwable, String message) {
        this.throwable = throwable;
        this.message = message;
    }
}
