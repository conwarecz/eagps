package net.aineuron.eagps.activity;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.model.StateManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@EActivity(R.layout.activity_state_settings)
public class StateSettingsActivity extends AppCompatActivity {

	@Bean
	StateManager stateManager;

	@EventBusGreenRobot
	EventBus bus;

	private MaterialDialog progressDialog;

	@AfterViews
	public void afterViews() {
		getSupportActionBar().hide();
	}

	@Click(R.id.readyLayout)
	public void onReady() {
		stateManager.setStateReady();
		showProgress();
	}

	@Click(R.id.busyLayout)
	public void onBusy() {
		stateManager.setStateBusy();
		showProgress();
	}

	@Click(R.id.unavailableLayout)
	public void onUnavailable() {
		stateManager.setStateUnavailable();
		showProgress();
	}

	@Click(R.id.skipLayout)
	public void onSkip() {
		// Stays same state
		finishSettings();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkStateSelectedEvent(StateSelectedEvent e) {
		progressDialog.dismiss();
		finishSettings();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkStateSelectedEvent(ApiErrorEvent e) {
		progressDialog.dismiss();
		Toast.makeText(this, e.throwable.getMessage(), Toast.LENGTH_SHORT).show();
	}

	private void showProgress() {
		progressDialog = new MaterialDialog.Builder(this)
				.title("Měním stav")
				.content("Prosím čekejte...")
				.cancelable(false)
				.progress(true, 0)
				.show();
	}

	private void finishSettings() {
		MainActivity_.intent(this).start();
		finish();
	}
}
