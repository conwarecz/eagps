package net.aineuron.eagps.model.viewmodel;

import net.aineuron.eagps.model.database.order.PhotoPathsWithReason;

/**
 * Created by Vit Veres on 11-Jun-17
 * as a part of Android-EAGPS project.
 */

public class PhotoPathsWithReasonViewModel {
	public final PhotoPathsWithReason photoPathsWithReason;
	public final int itemType;
	public int photoIndex = -1;
	public String photoPath;

	public PhotoPathsWithReasonViewModel(int itemType, PhotoPathsWithReason photoPathsWithReason) {
		this.itemType = itemType;
		this.photoPathsWithReason = photoPathsWithReason;
	}

	public PhotoPathsWithReasonViewModel withPhotoPath(int index, String photoPath) {
		this.photoIndex = index;
		this.photoPath = photoPath;

		return this;
	}
}
