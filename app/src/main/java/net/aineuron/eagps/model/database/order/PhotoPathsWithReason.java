package net.aineuron.eagps.model.database.order;

import net.aineuron.eagps.model.database.RealmString;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Vit Veres on 06-Jun-17
 * as a part of Android-EAGPS project.
 */

public class PhotoPathsWithReason extends RealmObject {
	private String reasonForNoPhotos;
	private RealmList<RealmString> photoPaths = new RealmList<>();

	public String getReasonForNoPhotos() {
		return reasonForNoPhotos;
	}

	public void setReasonForNoPhotos(String reasonForNoPhotos) {
		this.reasonForNoPhotos = reasonForNoPhotos;
	}

	public RealmList<RealmString> getPhotoPaths() {
		return photoPaths;
	}

	public void setPhotoPaths(RealmList<RealmString> photoPaths) {
		this.photoPaths = photoPaths;
	}
}
