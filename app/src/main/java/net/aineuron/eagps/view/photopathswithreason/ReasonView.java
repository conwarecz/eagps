package net.aineuron.eagps.view.photopathswithreason;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.EditText;

import net.aineuron.eagps.R;

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

	public ReasonView(@NonNull Context context) {
		super(context);
	}

	@Override
	protected void bindView() {
		reason.setText(item.photoPathsWithReason.getReasonForNoPhotos());
	}

	@TextChange(R.id.reason)
	void onReasonChanged() {
		String reasonText = reason.getText().toString();

		item.photoPathsWithReason.setReasonForNoPhotos(reasonText);
	}
}
