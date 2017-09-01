package net.aineuron.eagps.event.ui;

/**
 * Created by Vit Veres on 16-May-17
 * as a part of Android-EAGPS project.
 */

public class WorkerCarSelectedEvent {
    public final Long selectedCarId;
    public final boolean isSelected;

    public WorkerCarSelectedEvent(long selectedCarId, boolean isSelected) {
        this.selectedCarId = selectedCarId;
        this.isSelected = isSelected;
    }
}
