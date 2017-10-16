package net.aineuron.eagps.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.aineuron.eagps.model.database.order.PhotoPathsWithReason;
import net.aineuron.eagps.model.viewmodel.PhotoPathsWithReasonViewModel;
import net.aineuron.eagps.view.ItemViewWrapper;
import net.aineuron.eagps.view.photopathswithreason.AddMorePhotosView_;
import net.aineuron.eagps.view.photopathswithreason.AddPhotoView_;
import net.aineuron.eagps.view.photopathswithreason.AttachmentPhotoView_;
import net.aineuron.eagps.view.photopathswithreason.BasePhotoPathsWithReasonView;
import net.aineuron.eagps.view.photopathswithreason.ReasonView_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;

import static net.aineuron.eagps.view.photopathswithreason.BasePhotoPathsWithReasonView.TYPE_ADD_MORE_PHOTOS;
import static net.aineuron.eagps.view.photopathswithreason.BasePhotoPathsWithReasonView.TYPE_ADD_PHOTOS;
import static net.aineuron.eagps.view.photopathswithreason.BasePhotoPathsWithReasonView.TYPE_PHOTO;
import static net.aineuron.eagps.view.photopathswithreason.BasePhotoPathsWithReasonView.TYPE_REASON;

/**
 * Created by Vit Veres on 11-Jun-17
 * as a part of Android-EAGPS project.
 */

@EBean
public class PhotoPathsWithReasonAdapter extends BaseRecyclerViewAdapter<PhotoPathsWithReasonViewModel, BasePhotoPathsWithReasonView> {

	@RootContext
	Context context;

	private int addPhotoTarget = -1;
	private PhotoPathsWithReason photoPathsWithReason;

	public PhotoPathsWithReasonAdapter withPhotoPaths(PhotoPathsWithReason photoPathsWithReason) {
		this.photoPathsWithReason = photoPathsWithReason;
		return this;
	}

	public PhotoPathsWithReasonAdapter withAddPhotoTargetId(int addPhotoTarget) {
		this.addPhotoTarget = addPhotoTarget;
		return this;
	}

	public PhotoPathsWithReasonAdapter finish() {
		notifyDataChanged();
		return this;
	}

	@Override
	protected BasePhotoPathsWithReasonView onCreateItemView(ViewGroup parent, int viewType) {
		switch (viewType) {
			case TYPE_PHOTO:
				return AttachmentPhotoView_.build(context);
			case TYPE_ADD_PHOTOS:
				return AddPhotoView_.build(context);
			case TYPE_ADD_MORE_PHOTOS:
				return AddMorePhotosView_.build(context);
			case TYPE_REASON:
				return ReasonView_.build(context);
		}

		return null;
	}

	@Override
	public int getItemViewType(int position) {
		return items.get(position).itemType;
	}

	@Override
	public void onBindViewHolder(ItemViewWrapper<BasePhotoPathsWithReasonView> holder, int position) {
		BasePhotoPathsWithReasonView view = holder.getView();
		PhotoPathsWithReasonViewModel item = items.get(position);

		view.bind(item, addPhotoTarget);

		view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	private void notifyDataChanged() {
		items = new ArrayList<>();

		if (photoPathsWithReason.getPhotoPaths().size() == 0) {
			items.add(new PhotoPathsWithReasonViewModel(TYPE_ADD_PHOTOS, photoPathsWithReason).withAddPhotoTargetId(addPhotoTarget));

			// TODO: Odkomentovat, když bude za potřebí reason
//			items.add(new PhotoPathsWithReasonViewModel(TYPE_REASON, photoPathsWithReason));
			return;
		}

		for (int i = 0; i < photoPathsWithReason.getPhotoPaths().size(); i++) {
			items.add(new PhotoPathsWithReasonViewModel(TYPE_PHOTO, photoPathsWithReason).withPhotoPath(i, photoPathsWithReason.getPhotoPaths().get(i).getValue()));
		}
		items.add(new PhotoPathsWithReasonViewModel(TYPE_ADD_MORE_PHOTOS, photoPathsWithReason).withAddPhotoTargetId(addPhotoTarget));

		notifyDataSetChanged();
	}
}
