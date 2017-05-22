package net.aineuron.eagps.activity;

import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.adapter.WorkerSelectCarAdapter;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.ui.WorkerCarSelectedEvent;

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
	WorkerSelectCarAdapter carAdapter;

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
		MainActivity.STATE = MainActivity.STATE_BUSY;
		finishSettings();
	}

	@Click(R.id.unavailableLayout)
	public void onUnavailable() {
		MainActivity.STATE = MainActivity.STATE_UNAVAILABLE;
		finishSettings();
	}

	@Click(R.id.readyLayout)
	public void onReady() {
		MainActivity.STATE = MainActivity.STATE_READY;
		finishSettings();
	}

	@Click(R.id.skipLayout)
	public void onSkip() {
		// Stays same state
		finishSettings();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCarSelectedEvent(WorkerCarSelectedEvent e) {
		progressDialog = new MaterialDialog.Builder(this)
				.title("Vybírám auto")
				.content("Prosím čekejte...")
				.cancelable(false)
				.progress(true, 0)
				.show();
		clientProvider.getEaClient().selectCar(e.selectedCarId);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkCarSelectedEvent(CarSelectedEvent e) {
		progressDialog.dismiss();
		finishSettings();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkCarSelectedEvent(ApiErrorEvent e) {
		carAdapter.notifyDataChanged();
		progressDialog.dismiss();
	}

	private void finishSettings() {
		MainActivity_.intent(this).start();
		finish();
	}
}
