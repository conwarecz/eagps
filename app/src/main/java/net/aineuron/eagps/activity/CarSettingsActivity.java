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
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.ui.WorkerCarSelectedEvent;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.util.IntentUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_car_settings)
public class CarSettingsActivity extends AppCompatActivity {

	@ViewById(R.id.carsView)
	RecyclerView carsView;

	@ViewById(R.id.carsRefresh)
	SwipeRefreshLayout carsRefresh;

	@Bean
	WorkerSelectCarAdapter carAdapter;

	@Bean
	UserManager userManager;

	@Bean
	ClientProvider clientProvider;

	@EventBusGreenRobot
	EventBus bus;

	@Extra
	boolean resetCar;

	private MaterialDialog progressDialog;
	private List<Car> cars = new ArrayList<>();

	@AfterViews
	public void afterViews() {
		getSupportActionBar().hide();

		User user = userManager.getUser();
		if (user == null) {
			Toast.makeText(this, "No User", Toast.LENGTH_SHORT).show();
			return;
		}

        if (user.getEntity() != null) {
            Long carId = user.getEntity().getEntityId();
            if (carId != null && !resetCar) {
                // Car Is already selected
                finishSettings();
            }
        }

		carsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		carsView.setAdapter(carAdapter);
		carsRefresh.setOnRefreshListener(() -> carsRefresh.setRefreshing(false));

		carsRefresh.setRefreshing(true);
		clientProvider.getEaClient().getCars();
	}

	@Click(R.id.skipLayout)
	public void onSkip() {
		userManager.setStateNoCar();
		progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.dialog_changing_settings)
                .content(getString(R.string.dialog_wait_content))
                .cancelable(false)
                .progress(true, 0)
                .show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCarSelectedEvent(WorkerCarSelectedEvent e) {
		progressDialog = new MaterialDialog.Builder(this)
				.title("Vybírám auto")
                .content(getString(R.string.dialog_wait_content))
                .cancelable(false)
                .progress(true, 0)
                .show();
		userManager.selectCar(e.selectedCarId);
        userManager.setSelectedStateId(e.stateId);
    }

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkCarSelectedEvent(CarSelectedEvent e) {
		progressDialog.dismiss();
		finishSettings();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCarsDownloaded(CarsDownloadedEvent e) {
		carsRefresh.setRefreshing(false);
		this.cars = e.cars;
		carAdapter.setCars(e.cars);
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
	public void onApiError(ApiErrorEvent e) {
		carAdapter.notifyDataChanged();
		carsRefresh.setRefreshing(false);
		try {
			progressDialog.dismiss();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Toast.makeText(this, e.throwable.getMessage(), Toast.LENGTH_SHORT).show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCarSelectError(KnownErrorEvent e) {
		progressDialog.dismiss();
		Toast.makeText(this, e.knownError.getMessage(), Toast.LENGTH_SHORT).show();
	}

	private void finishSettings() {
		IntentUtils.openMainActivity(this);
		finish();
	}
}
