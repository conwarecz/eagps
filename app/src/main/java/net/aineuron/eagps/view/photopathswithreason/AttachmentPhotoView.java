package net.aineuron.eagps.view.photopathswithreason;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.event.ui.RemovePhotoEvent;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by Vit Veres on 11-Jun-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.item_attachement_photo)
public class AttachmentPhotoView extends BasePhotoPathsWithReasonView {

	@ViewById(R.id.photoView)
	ImageView photoView;

	@EventBusGreenRobot
	EventBus bus;

	public AttachmentPhotoView(@NonNull Context context) {
		super(context);
	}

	@Override
	protected void bindView() {
		String photoPath = item.photoPath;
		Glide.with(getContext()).load(photoPath).into(photoView);
	}

	@Click(R.id.removePhoto)
	void photosClicked() {
		new MaterialDialog.Builder(getContext())
				.title("Smazat")
				.content("Opravdu chcete smazat fotku nebo dokument?")
				.positiveText("Ano")
				.negativeText("Zrušit")
				.onPositive((dialog, which) -> bus.post(new RemovePhotoEvent(item.photos, item.photoPath)))
				.show();
	}
}
