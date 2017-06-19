package net.aineuron.eagps.view.photopathswithreason;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 11-Jun-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.item_attachement_photo)
public class AttachmentPhotoView extends BasePhotoPathsWithReasonView {

	@ViewById(R.id.photoView)
	ImageView photoView;

	public AttachmentPhotoView(@NonNull Context context) {
		super(context);
	}

	@Override
	protected void bindView() {
		String photoPath = item.photoPath;
		Glide.with(getContext()).load(photoPath).into(photoView);
	}

	@Click(R.id.photoView)
	void photosClicked() {
		// TODO:
		Toast.makeText(getContext(), "Photo clicked", Toast.LENGTH_SHORT).show();
	}
}
