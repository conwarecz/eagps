package net.aineuron.eagps.model.viewmodel;

import net.aineuron.eagps.model.database.order.Photo;

import java.util.List;

/**
 * Created by Vit Veres on 11-Jun-17
 * as a part of Android-EAGPS project.
 */

public class PhotoPathsWithReasonViewModel {
    public final List<Photo> photos;
    public final int itemType;
	public int photoIndex = -1;
	public String photoPath;
	public int addPhotoTarget = -1;
    public String reason;

    public PhotoPathsWithReasonViewModel(int itemType, List<Photo> photos) {
        this.itemType = itemType;
        this.photos = photos;
    }

	public PhotoPathsWithReasonViewModel withPhotoPath(int index, String photoPath) {
		this.photoIndex = index;
		this.photoPath = photoPath;

		return this;
	}

	public PhotoPathsWithReasonViewModel withAddPhotoTargetId(int addPhotoTarget) {
		this.addPhotoTarget = addPhotoTarget;
		return this;
	}

    public PhotoPathsWithReasonViewModel withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
