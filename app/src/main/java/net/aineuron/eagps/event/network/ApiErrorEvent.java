package net.aineuron.eagps.event.network;

/**
 * Created by Vit Veres on 19.2.2016
 * as a part of AlTraceabilitySystem project.
 */
public class ApiErrorEvent {
	public final Throwable throwable;

	public ApiErrorEvent(Throwable throwable) {
		this.throwable = throwable;
	}
}
