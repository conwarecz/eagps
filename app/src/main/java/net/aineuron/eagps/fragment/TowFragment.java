package net.aineuron.eagps.fragment;

import android.widget.ImageView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivity;
import net.aineuron.eagps.activity.MainActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_tow)
public class TowFragment extends BaseFragment {

	@ViewById(R.id.stateImage)
	ImageView stateImage;

	public static TowFragment newInstance() {
		return TowFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarTitle("Odtah");
		setContent();
	}

	@Click(R.id.stateImage)
	void finishClicked() {
		MainActivity.STATE = MainActivity.STATE_READY;
		MainActivity_.intent(this).start();
		getActivity().finish();
	}

	private void setContent() {
		stateImage.setImageResource(R.drawable.busy_order);
	}
}
