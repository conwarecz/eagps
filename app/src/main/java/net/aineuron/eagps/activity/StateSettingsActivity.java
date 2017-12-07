package net.aineuron.eagps.activity;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.user.UserLoggedOutFromAnotherDeviceEvent;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.util.IntentUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@EActivity(R.layout.activity_state_settings)
public class StateSettingsActivity extends AppCompatActivity {

	@Bean
	UserManager userManager;

	@EventBusGreenRobot
	EventBus bus;

	@Nullable
	@Extra
	Long carStatus;

	private MaterialDialog progressDialog;

	@AfterViews
	public void afterViews() {
		getSupportActionBar().hide();
	}

	@Click(R.id.readyLayout)
	public void onReady() {
		showProgress();
		userManager.setStateReady();
	}

	@Click(R.id.busyLayout)
	public void onBusy() {
		showProgress();
		userManager.setStateBusy();
	}

	@Click(R.id.unavailableLayout)
	public void onUnavailable() {
		showProgress();
		userManager.setStateUnavailable();
	}

	@Click(R.id.skipLayout)
	public void onSkip() {
		// Stays same state
		if (carStatus != null) {
			userManager.setSelectedStateId(carStatus);
		}
		finishSettings();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkStateSelectedEvent(StateSelectedEvent e) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		finishSettings();
	}
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onErrorApiEvent(ApiErrorEvent e) {
		dismissProgress();
		Toast.makeText(this, e.message, Toast.LENGTH_LONG).show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onKnownError(KnownErrorEvent e) {
		dismissProgress();
		Toast.makeText(this, e.knownError.getMessage(), Toast.LENGTH_LONG).show();
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserLoggedOut(UserLoggedOutFromAnotherDeviceEvent e) {
        Toast.makeText(this, "Byl jste odhlášen", Toast.LENGTH_LONG).show();
    }

	private void showProgress() {
		progressDialog = new MaterialDialog.Builder(this)
				.title("Měním stav")
                .content(getString(R.string.dialog_wait_content))
                .cancelable(false)
                .progress(true, 0)
                .show();
	}

	private void finishSettings() {
		IntentUtils.openMainActivity(this);
		finish();
	}

	protected void dismissProgress() {
		if (progressDialog == null) {
			return;
		}

		if (progressDialog.isCancelled()) {
			return;
		}

		progressDialog.dismiss();
	}
}
