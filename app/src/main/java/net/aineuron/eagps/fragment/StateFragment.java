package net.aineuron.eagps.fragment;

import android.widget.ImageView;
import android.widget.TextView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.StateSettingsActivity_;
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
public class StateFragment extends BaseFragment {

	@ViewById(R.id.stateIcon)
	ImageView stateIcon;

	@ViewById(R.id.stateText)
	TextView stateText;

	@ViewById(R.id.stateSubtext)
	TextView stateSubtext;

	@Bean
	UserManager userManager;

	public static StateFragment newInstance() {
		return StateFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(false);
		setAppbarTitle("Zásah");
		setContent();
	}

	@Click(R.id.changeButton)
	void stateImageClicked() {
		StateSettingsActivity_.intent(getContext()).start();
	}

	private void setContent() {
		Long i = userManager.getSelectedStateId();
		if (i == null) {
			setErrorState();
		} else if (i.equals(UserManager.STATE_ID_READY)) {
			setReadyContent();

		} else if (i.equals(UserManager.STATE_ID_BUSY)) {
			setBusyContent();

		} else if (i.equals(UserManager.STATE_ID_UNAVAILABLE)) {
			setUnavailableContent();

		} else if (i.equals(UserManager.STATE_ID_NO_CAR)) {
			setNoCarContent();

		} else {
			setErrorState();
		}
	}

	private void setReadyContent() {
		stateText.setText("Čekání na přidělení zakázky");
		stateIcon.setImageResource(R.drawable.icon_big_waiting);
	}

	private void setBusyContent() {
		stateText.setText("Zaneprázdněn");
		stateSubtext.setText("Nejste připraven na zakázku a nemůžete být poptáni pro zásahy EA");
		stateIcon.setImageResource(R.drawable.icon_big_busy);
	}

	private void setUnavailableContent() {
		stateText.setText("Nedostupný");
		stateSubtext.setText("Při statusu nedostupný nejste v systémech EA viditelní (např. vozidlo v servise, atd.)");
		stateIcon.setImageResource(R.drawable.icon_big_unavailable);
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
