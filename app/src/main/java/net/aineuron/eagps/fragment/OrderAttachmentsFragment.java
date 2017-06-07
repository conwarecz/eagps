package net.aineuron.eagps.fragment;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

/**
 * Created by Vit Veres on 07-Jun-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragnent_order_attachments)
public class OrderAttachmentsFragment extends BaseFragment {
	public static OrderAttachmentsFragment newInstance() {
		return OrderAttachmentsFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarTitle("Přílohy");
	}
}
