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

	@Click(R.id.settingsImage)
	public void imageClick() {
		MainActivity_.intent(this).start();
		finish();
	}
}
