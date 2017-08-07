package net.aineuron.eagps.fragment;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.CarSettingsActivity_;
import net.aineuron.eagps.model.UserManager;

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
public class NoCarStateFragment extends BaseFragment {

	@ViewById(R.id.stateIcon)
	ImageView stateIcon;

	@ViewById(R.id.stateText)
	TextView stateText;

	@ViewById(R.id.stateSubtext)
	TextView stateSubtext;

	@ViewById(R.id.changeButton)
	Button changeButton;

	@Bean
	UserManager userManager;

	public static NoCarStateFragment newInstance() {
		return NoCarStateFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(false);
		setAppbarTitle("Zásah");
		setContent();
	}

	@Click(R.id.changeButton)
	void stateImageClicked() {
		CarSettingsActivity_.intent(getContext()).resetCar(true).start();
		getActivity().finish();
	}

	private void setContent() {
		changeButton.setText("Změnit vozidlo");
		Long i = userManager.getSelectedStateId();
		if (i == null) {
			setErrorState();
		} else if (i.equals(UserManager.STATE_ID_NO_CAR)) {
			setNoCarContent();

		} else {
			setErrorState();
		}
	}

	private void setNoCarContent() {
		stateText.setText("Vozidlo nevybráno");
		stateIcon.setImageResource(R.drawable.icon_big_notselected);
	}

	private void setErrorState() {
		stateText.setText("Neznámý stav");
		stateIcon.setImageResource(R.drawable.icon_big_busy);
	}
}