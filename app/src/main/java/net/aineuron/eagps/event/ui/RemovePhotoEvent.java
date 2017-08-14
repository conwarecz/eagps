package net.aineuron.eagps.event.ui;

import net.aineuron.eagps.model.database.order.PhotoPathsWithReason;

/**
 * Created by Vit Veres on 10.08.2017
 * as a part of eagps project.
 */

public class RemovePhotoEvent {
	public final PhotoPathsWithReason photoPathsWithReason;
	public final String photoPath;

	public RemovePhotoEvent(PhotoPathsWithReason photoPathsWithReason, String photoPath) {
		this.photoPath = photoPath;
		this.photoPathsWithReason = photoPathsWithReason;
	}
}
