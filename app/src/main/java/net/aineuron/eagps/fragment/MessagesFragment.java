package net.aineuron.eagps.fragment;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_messages)
public class MessagesFragment extends BaseFragment {

	public static MessagesFragment newInstance() {
		return MessagesFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarTitle("Zprávy");
	}
}
