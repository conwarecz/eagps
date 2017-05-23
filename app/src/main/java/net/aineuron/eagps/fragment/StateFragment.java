package net.aineuron.eagps.fragment;

import android.widget.ImageView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.StateSettingsActivity_;
import net.aineuron.eagps.model.StateManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_state)
public class StateFragment extends BaseFragment {

	@ViewById(R.id.stateImage)
	ImageView stateImage;

	@Bean
	StateManager stateManager;

	public static StateFragment newInstance() {
		return StateFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarTitle("Zásah");
		setContent();
	}

	@Click(R.id.stateImage)
	void stateImageClicked() {
		StateSettingsActivity_.intent(getContext()).start();
		getActivity().finish();
	}

	private void setContent() {
		switch (stateManager.getSelectedStateId()) {
			case StateManager.STATE_ID_READY:
				stateImage.setImageResource(R.drawable.ready);
				break;
			case StateManager.STATE_ID_BUSY:
				stateImage.setImageResource(R.drawable.busy);
				break;
			case StateManager.STATE_ID_UNAVAILABLE:
				stateImage.setImageResource(R.drawable.unavailable);
				break;
			case StateManager.STATE_ID_NO_CAR:
				stateImage.setImageResource(R.drawable.no_car);
				break;
			default:
				stateImage.setImageBitmap(null);
				break;
		}
	}
}
