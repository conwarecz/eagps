package net.aineuron.eagps.view.photopathswithreason;

import android.content.Context;
import android.support.annotation.NonNull;

import net.aineuron.eagps.model.viewmodel.PhotoPathsWithReasonViewModel;
import net.aineuron.eagps.view.BaseItemView;

/**
 * Created by Vit Veres on 11-Jun-17
 * as a part of Android-EAGPS project.
 */

public abstract class BasePhotoPathsWithReasonView extends BaseItemView<PhotoPathsWithReasonViewModel> {
	public static final int TYPE_ADD_PHOTOS = 0;
	public static final int TYPE_ADD_MORE_PHOTOS = 1;
	public static final int TYPE_REASON = 2;
	public static final int TYPE_PHOTO = 3;

	public BasePhotoPathsWithReasonView(@NonNull Context context) {
		super(context);
	}
}
