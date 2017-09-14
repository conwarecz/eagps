package net.aineuron.eagps.event.network;

import net.aineuron.eagps.model.transfer.KnownError;

/**
 * Created by Vit Veres on 16.08.2017
 * as a part of eagps project.
 */

public class KnownErrorEvent {
	public final KnownError knownError;

	public KnownErrorEvent(KnownError knownError) {
		this.knownError = knownError;
	}
}
