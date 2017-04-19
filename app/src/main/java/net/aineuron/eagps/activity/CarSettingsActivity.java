package net.aineuron.eagps.activity;

import android.support.v7.app.AppCompatActivity;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_car_settings)
public class CarSettingsActivity extends AppCompatActivity {

	@AfterViews
	public void afterViews() {
		getSupportActionBar().hide();
	}

	@Click(R.id.ready)
	public void ready() {
		MainActivity.STATE = MainActivity.STATE_READY;
		finishSettings();
	}

	@Click(R.id.busy)
	public void busy() {
		MainActivity.STATE = MainActivity.STATE_BUSY;
		finishSettings();
	}

	@Click(R.id.unavailable)
	public void unavailable() {
		MainActivity.STATE = MainActivity.STATE_UNAVAILABLE;
		finishSettings();
	}

	@Click(R.id.skip)
	public void skip() {
		MainActivity.STATE = MainActivity.STATE_NO_CAR;
		finishSettings();
	}

	private void finishSettings() {
		MainActivity_.intent(this).start();
		finish();
	}
}
