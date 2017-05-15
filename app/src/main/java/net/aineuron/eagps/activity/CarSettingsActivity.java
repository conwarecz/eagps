package net.aineuron.eagps.activity;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.adapter.WorkerSelectCarAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_car_settings)
public class CarSettingsActivity extends AppCompatActivity {

	@ViewById(R.id.carsView)
	RecyclerView carsView;

	@Bean
	WorkerSelectCarAdapter carAdapter;

	@AfterViews
	public void afterViews() {
		getSupportActionBar().hide();

		carsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		carsView.setAdapter(carAdapter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		carsView.setAdapter(null);
	}

	private void finishSettings() {
		MainActivity_.intent(this).start();
		finish();
	}
}
