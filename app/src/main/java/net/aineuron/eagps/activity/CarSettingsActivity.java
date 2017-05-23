package net.aineuron.eagps.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.adapter.WorkerSelectCarAdapter;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.ui.WorkerCarSelectedEvent;
import net.aineuron.eagps.model.CarsManager;
import net.aineuron.eagps.model.StateManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@EActivity(R.layout.activity_car_settings)
public class CarSettingsActivity extends AppCompatActivity {

	@ViewById(R.id.carsView)
	RecyclerView carsView;

	@ViewById(R.id.carsRefresh)
	SwipeRefreshLayout carsRefresh;

	@Bean
	WorkerSelectCarAdapter carAdapter;

	@Bean
	CarsManager carsManager;

	@Bean
	StateManager stateManager;

	@EventBusGreenRobot
	EventBus bus;

	private MaterialDialog progressDialog;

	@AfterViews
	public void afterViews() {
		getSupportActionBar().hide();
		carsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		carsView.setAdapter(carAdapter);
		carsRefresh.setOnRefreshListener(() -> carsRefresh.setRefreshing(false));
	}

	@Click(R.id.skipLayout)
	public void onSkip() {
		stateManager.setStateNoCar();
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
		carsManager.selectCar(e.selectedCarId);
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
		Toast.makeText(this, e.throwable.getMessage(), Toast.LENGTH_SHORT).show();
	}

	private void finishSettings() {
		StateSettingsActivity_.intent(this).start();
		finish();
	}
}
