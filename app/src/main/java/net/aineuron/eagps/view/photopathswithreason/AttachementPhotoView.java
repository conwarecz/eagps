package net.aineuron.eagps.view.photopathswithreason;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.Toast;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 11-Jun-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.item_attachement_photo)
public class AttachementPhotoView extends BasePhotoPathsWithReasonView {

	@ViewById(R.id.photoView)
	ImageView photoView;

	public AttachementPhotoView(@NonNull Context context) {
		super(context);
	}

	@Override
	protected void bindView() {

	}

	@Click(R.id.photoView)
	void addMorePhotosClicked() {
		// TODO:
		Toast.makeText(getContext(), "Add Photo clicked", Toast.LENGTH_SHORT).show();
	}
}
