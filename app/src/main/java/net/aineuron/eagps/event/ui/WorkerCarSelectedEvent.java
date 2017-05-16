package net.aineuron.eagps.event.ui;

/**
 * Created by Vit Veres on 16-May-17
 * as a part of Android-EAGPS project.
 */

public class WorkerCarSelectedEvent {
	public final long selectedCarId;

	public WorkerCarSelectedEvent(long selectedCarId) {
		this.selectedCarId = selectedCarId;
	}
}
