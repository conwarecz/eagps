package net.aineuron.eagps.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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
	ClientProvider clientProvider;

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

	@Click(R.id.skipButton)
	public void onSkip() {
		// TODO: Make state no car
		MainActivity.STATE = MainActivity.STATE_NO_CAR;
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
		progressDialog.dismiss();
	}

	private void finishSettings() {
		MainActivity_.intent(this).start();
		finish();
	}
}
