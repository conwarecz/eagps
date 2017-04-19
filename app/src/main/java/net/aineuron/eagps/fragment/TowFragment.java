package net.aineuron.eagps.fragment;

import android.widget.ImageView;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterViews;
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

	private void setContent() {
		stateImage.setImageResource(R.drawable.busy_order);
	}
}
