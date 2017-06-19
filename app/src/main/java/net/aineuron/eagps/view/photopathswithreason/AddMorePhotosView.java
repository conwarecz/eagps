package net.aineuron.eagps.view.photopathswithreason;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.event.ui.AddPhotoEvent;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by Vit Veres on 11-Jun-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.item_attachement_add_more_photos)
public class AddMorePhotosView extends BasePhotoPathsWithReasonView {

	@ViewById(R.id.addMorePhotos)
	ImageView imageView;

	public AddMorePhotosView(@NonNull Context context) {
		super(context);
	}

	@Override
	protected void bindView() {

	}

	@Click(R.id.addMorePhotos)
	void addMorePhotosClicked() {
		EventBus.getDefault().post(new AddPhotoEvent(item.addPhotoTarget));
	}
}
