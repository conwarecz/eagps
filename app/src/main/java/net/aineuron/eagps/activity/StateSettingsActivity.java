package net.aineuron.eagps.activity;

import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;

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
	ClientProvider clientProvider;

	@EventBusGreenRobot
	EventBus bus;

	private MaterialDialog progressDialog;

	@AfterViews
	public void afterViews() {
		getSupportActionBar().hide();
	}

	@Click(R.id.busyLayout)
	public void onBusy() {
		selectState(MainActivity.STATE_BUSY);
	}

	@Click(R.id.unavailableLayout)
	public void onUnavailable() {
		selectState(MainActivity.STATE_UNAVAILABLE);
	}

	@Click(R.id.readyLayout)
	public void onReady() {
		selectState(MainActivity.STATE_READY);
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
	}

	private void selectState(String state) {
		progressDialog = new MaterialDialog.Builder(this)
				.title("Vybírám stav")
				.content("Prosím čekejte...")
				.cancelable(false)
				.progress(true, 0)
				.show();
		clientProvider.getEaClient().setState(state);
	}

	private void finishSettings() {
		MainActivity_.intent(this).start();
		finish();
	}
}
