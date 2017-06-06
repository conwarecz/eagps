package net.aineuron.eagps.model.database.order;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vit Veres on 06-Jun-17
 * as a part of Android-EAGPS project.
 */

public class PhotoPathsWithReason {
	private String reasonForNoPhotos;
	private List<String> photoPaths = new ArrayList<>();

	public String getReasonForNoPhotos() {
		return reasonForNoPhotos;
	}

	public void setReasonForNoPhotos(String reasonForNoPhotos) {
		this.reasonForNoPhotos = reasonForNoPhotos;
	}

	public List<String> getPhotoPaths() {
		return photoPaths;
	}

	public void setPhotoPaths(List<String> photoPaths) {
		this.photoPaths = photoPaths;
	}
}
