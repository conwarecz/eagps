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

@EViewGroup(R.layout.item_attachement_add_photos)
public class AddPhotoView extends BasePhotoPathsWithReasonView {

	@ViewById(R.id.addPhotos)
	ImageView imageView;

	public AddPhotoView(@NonNull Context context) {
		super(context);
	}

	@Override
	protected void bindView() {
		String photoPath = item.photoPath;
		Glide.with(this).load(photoPath).into(imageView);
	}

	@Click(R.id.addPhotos)
	void addMorePhotosClicked() {
		// TODO:
		Toast.makeText(getContext(), "Add photos clicked", Toast.LENGTH_SHORT).show();
	}
}
