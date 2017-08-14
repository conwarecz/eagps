package net.aineuron.eagps.view.photopathswithreason;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.EditText;
import android.widget.TextView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.fragment.OrderAttachmentsFragment;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 11-Jun-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.item_attachement_reason)
public class ReasonView extends BasePhotoPathsWithReasonView {

	@ViewById(R.id.reason)
	EditText reason;

	@ViewById(R.id.title)
	TextView title;

	public ReasonView(@NonNull Context context) {
		super(context);
	}

	@Override
	protected void bindView() {
		if (targetId == OrderAttachmentsFragment.REQUEST_CODE_CHOOSE_DOCS) {
			title.setText("Důvod nepořízení zakázkového listu");
			reason.setHint("Nahrát zakázkový list");
		}
		reason.setText(item.photoPathsWithReason.getReasonForNoPhotos());
	}

	@TextChange(R.id.reason)
	void onReasonChanged() {
		String reasonText = reason.getText().toString();

		item.photoPathsWithReason.setReasonForNoPhotos(reasonText);
	}
}
