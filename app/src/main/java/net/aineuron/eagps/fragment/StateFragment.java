package net.aineuron.eagps.fragment;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.AppBarActivity;
import net.aineuron.eagps.activity.StateSettingsActivity_;
import net.aineuron.eagps.event.network.user.UserDataGotEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.util.IntentUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;

import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY_ORDER;
import static net.aineuron.eagps.model.UserManager.STATE_ID_NO_CAR;
import static net.aineuron.eagps.model.UserManager.STATE_ID_READY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_UNAVAILABLE;

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

	@ViewById(R.id.changeButton)
	Button stateButton;

	@Bean
	UserManager userManager;

	@Bean
	OrdersManager ordersManager;

	private Long actualState = -1L;
	private boolean alreadyTried = false;

	public static StateFragment newInstance() {
		return StateFragment_.builder().build();
	}

	public Long getActualState() {
		return actualState;
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(false);
		AppBarActivity activity = (AppBarActivity) getActivity();
		if (activity != null) {
			activity.setUpActionBar();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			setContent();
		} catch (Exception e) {
			Crashlytics.logException(e);
			e.printStackTrace();
		}
	}

	@Click(R.id.changeButton)
	void stateImageClicked() {
		StateSettingsActivity_.intent(getContext()).start();
	}

	@Subscribe
	public void onUserDataRefreshed(UserDataGotEvent e) {
		setContent();
	}

	private void setContent() {
		actualState = userManager.getSelectedStateId();
		stateButton.setVisibility(View.VISIBLE);
		if (actualState == null) {
			setErrorState();
		} else if (actualState.equals(STATE_ID_READY)) {
			setReadyContent();

		} else if (actualState.equals(STATE_ID_BUSY)) {
			setBusyContent();

		} else if (actualState.equals(STATE_ID_BUSY_ORDER)) {
			stateButton.setVisibility(View.INVISIBLE);
			if (userManager.haveActiveOrder()) {
//				((MainActivityBase) getActivity()).showFragment(TowFragment_.newInstance(ordersManager.getFirstActiveOrder().getId()), false);
				IntentUtils.openMainActivity(getContext());
			} else {
				if (!alreadyTried) {
					alreadyTried = true;
					userManager.getUserData(userManager.getUser().getUserId());
				}
				setOnOrderContent();
			}

		} else if (actualState.equals(STATE_ID_UNAVAILABLE)) {
			setUnavailableContent();

		} else if (actualState.equals(STATE_ID_NO_CAR)) {
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

    private void setOnOrderContent() {
        stateText.setText("Na zakázce");
        stateSubtext.setText("Aktuálně jste přiřazen na zakázce");
        stateIcon.setImageResource(R.drawable.icon_big_busy);
    }

	private void setUnavailableContent() {
		stateText.setText("Nedostupný");
		stateSubtext.setText("Při statusu nedostupný nejste v systémech EA viditelní (např. vozidlo v servise, atd.)");
		stateIcon.setImageResource(R.drawable.icon_big_unavailable);
	}

	private void setNoCarContent() {
		IntentUtils.openMainActivity(getContext());
//		stateText.setText("Vozidlo nevybráno");
//		stateIcon.setImageResource(R.drawable.icon_big_notselected);
	}

	private void setErrorState() {
		userManager.logout(userManager.getUser());
		Log.e("State", "UNKNOWN");
//		stateText.setText("Neznámý stav");
//		stateIcon.setImageResource(R.drawable.icon_big_busy);
	}
}
