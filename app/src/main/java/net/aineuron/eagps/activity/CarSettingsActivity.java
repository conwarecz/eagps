package net.aineuron.eagps.activity;

import android.support.annotation.Nullable;
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
import net.aineuron.eagps.event.network.car.CarReleasedEvent;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.ui.WorkerCarSelectedEvent;
import net.aineuron.eagps.model.OrdersManager;
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

import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY_ORDER;
import static net.aineuron.eagps.model.UserManager.STATE_ID_NO_CAR;

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
	OrdersManager ordersManager;

	@Bean
	ClientProvider clientProvider;

	@EventBusGreenRobot
	EventBus bus;

	@Extra
	boolean resetCar;

	@Nullable
	@Extra
	Long carStatus;

	private MaterialDialog progressDialog;
	private List<Car> cars = new ArrayList<>();
	private Long selectedCarState = null;

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
		progressDialog = new MaterialDialog.Builder(this)
				.title(R.string.dialog_changing_settings)
				.content(getString(R.string.dialog_wait_content))
				.cancelable(false)
				.progress(true, 0)
				.show();
		userManager.releaseCar(userManager.getSelectedCarId());
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCarReleasedEvent(CarReleasedEvent e) {
		if (e.selectedCarId == null) {
			progressDialog.dismiss();
			finishSettings();
		} else {
			userManager.selectCar(e.selectedCarId);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCarSelectedEvent(WorkerCarSelectedEvent e) {
		selectedCarState = e.stateId;
		boolean isAssigned = false;
        for (Car car : cars) {
            if (car.getId().equals(e.selectedCarId)) {
                if (car.getUserUsername() == null || car.getUserUsername().isEmpty() || car.getUserUsername().equalsIgnoreCase(userManager.getUser().getUserName())) {
                    continue;
                } else {
                    isAssigned = true;
                }
            }
        }
        if (isAssigned) {
            userOverride(e);
        } else {
            selectCar(e);
        }
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkCarSelectedEvent(CarSelectedEvent e) {
		if (!userManager.getSelectedStateId().equals(STATE_ID_NO_CAR)) {
			progressDialog.dismiss();
			if (e.carStateId.equals(STATE_ID_BUSY_ORDER)) {
				finishSettings();
			} else {
				selectState();
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkCarSelectedEvent(StateSelectedEvent e) {
		if (userManager.getSelectedStateId().equals(STATE_ID_NO_CAR)) {
			progressDialog.dismiss();
			finishSettings();
		}
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
        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show();
    }

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCarSelectError(KnownErrorEvent e) {
		progressDialog.dismiss();
		Toast.makeText(this, e.knownError.getMessage(), Toast.LENGTH_SHORT).show();
	}

	private void selectState() {
		if (!userManager.haveActiveOrder()) {
			StateSettingsActivity_.intent(this).extra("carStatus", selectedCarState).start();
		} else {
			IntentUtils.openMainActivity(this);
		}
		finish();
	}

	private void finishSettings() {
		IntentUtils.openMainActivity(this);
		finish();
	}

    private void selectCar(WorkerCarSelectedEvent e) {
        progressDialog = new MaterialDialog.Builder(this)
                .title("Vybírám auto")
                .content(getString(R.string.dialog_wait_content))
                .cancelable(false)
                .progress(true, 0)
                .show();
		if (userManager.getSelectedCarId() != null && !userManager.getSelectedCarId().equals(e.selectedCarId)) {
			ordersManager.deleteOrders();
		}
		if (!userManager.getSelectedStateId().equals(STATE_ID_NO_CAR)) {
            userManager.releaseCar(e.selectedCarId);
        } else {
            userManager.setSelectedStateId(e.stateId);
            userManager.selectCar(e.selectedCarId);
        }
    }

    private void userOverride(WorkerCarSelectedEvent e) {
        new MaterialDialog.Builder(this)
                .title("Přihlášený uživatel")
                .content("Na tomto vozidle již je přihlášený jiný uživatel, opravdu jej chcete odhlásit?")
                .cancelable(false)
                .positiveText("Ano")
                .negativeText("Ne")
                .onPositive((dialog, which) -> selectCar(e))
                .onNegative((dialog, which) -> dialog.dismiss())
                .show();
    }
}
