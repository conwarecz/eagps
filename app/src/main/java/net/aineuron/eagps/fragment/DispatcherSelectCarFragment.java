package net.aineuron.eagps.fragment;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import net.aineuron.eagps.R;
import net.aineuron.eagps.adapter.DispatcherSelectCarAdapter;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.event.network.car.CarStatusChangedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.network.car.DispatcherRefreshCarsEvent;
import net.aineuron.eagps.event.ui.WorkerCarSelectedEvent;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Order;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_READY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_UNAVAILABLE;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 31.08.2017.
 */

@EFragment(R.layout.fragment_dispatcher_car_select)
public class DispatcherSelectCarFragment extends BaseFragment {

    @ViewById(R.id.carsView)
    RecyclerView carsView;

    @ViewById(R.id.carsRefresh)
    SwipeRefreshLayout carsRefresh;

    @ViewById(R.id.carsAllChecker)
    AppCompatCheckBox allchecker;

    @Bean
    ClientProvider clientProvider;

    @Bean
    UserManager userManager;

    @Bean
    DispatcherSelectCarAdapter carAdapter;

    private Realm db;
    private RealmResults<Order> ordersRealmQuery;
    private List<Car> cars = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private List<Car> selectedCars = new ArrayList<>();
    private long carsNewState = STATE_ID_READY;

    public static DispatcherSelectCarFragment newInstance() {
        return DispatcherSelectCarFragment_.builder().build();
    }

    @Click(R.id.carsReady)
    void readyClicked() {
        carsNewState = STATE_ID_READY;
        allchecker.setChecked(false);
        setCarsState();
    }

    @Click(R.id.carsBusy)
    void busyClicked() {
        carsNewState = STATE_ID_BUSY;
        allchecker.setChecked(false);
        setCarsState();
    }

    @Click(R.id.carsUnavailable)
    void unavailableClicked() {
        carsNewState = STATE_ID_UNAVAILABLE;
        allchecker.setChecked(false);
        setCarsState();
    }

    @Click(R.id.carsAllChecker)
    void allCheckClicked() {
        if (allchecker.isChecked()) {
            carAdapter.checkAll();
            selectedCars.addAll(cars);
        } else {
            carAdapter.uncheckAll();
            selectedCars.removeAll(cars);
        }
    }

    @AfterViews
    void afterViews() {
        setAppbarUpNavigation(false);
        setAppbarTitle("Správa vozidel");

        User user = userManager.getUser();
        if (user == null) {
            Toast.makeText(getContext(), "No User", Toast.LENGTH_SHORT).show();
            return;
        }
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        carsView.setLayoutManager(layoutManager);
        carsView.setAdapter(carAdapter);
        carsRefresh.setOnRefreshListener(() -> {
            allchecker.setChecked(false);
            clientProvider.getEaClient().getCars();
        });

        DividerItemDecoration decor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.horizontal_divider_gray);
        decor.setDrawable(horizontalDivider);

        carsView.addItemDecoration(decor);
        carsRefresh.setRefreshing(true);
        clientProvider.getEaClient().getCars();
    }

    @Override
    public void onResume() {
        super.onResume();
        carsRefresh.setRefreshing(true);
        allchecker.setChecked(false);
        clientProvider.getEaClient().getCars();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarsDownloaded(CarsDownloadedEvent e) {
        hideProgress();
        carsRefresh.setRefreshing(false);
        this.cars = e.cars;
        carAdapter.setCars(e.cars);
        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            AppCompatCheckBox checkBox = layoutManager.getChildAt(i).findViewById(R.id.carCheckRZ);
            checkBox.setChecked(false);
        }
        carAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkCarSelectedEvent(ApiErrorEvent e) {
        carsRefresh.setRefreshing(false);
        carAdapter.notifyDataChanged();
        Toast.makeText(getContext(), e.message, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarSelectError(KnownErrorEvent e) {
        carsRefresh.setRefreshing(false);
        Toast.makeText(getContext(), e.knownError.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarClicked(WorkerCarSelectedEvent e) {
        for (Car car : cars) {
            if (car.getId().equals(e.selectedCarId)) {
                if (e.isSelected) {
                    selectedCars.add(car);
                } else {
                    selectedCars.remove(car);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void carStatusChanged(CarStatusChangedEvent e) {
        for (Car car : selectedCars) {
            if (car.getId().equals(e.carId)) {
                car.setSent(true);
            }
            if (!car.isSent()) {
                changeCarState(car);
                return;
            }
        }
        selectedCars = new ArrayList<>();
        clientProvider.getEaClient().getCars();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshCars(DispatcherRefreshCarsEvent e) {
        carsRefresh.setRefreshing(true);
        clientProvider.getEaClient().getCars();
    }

    private void setCarsState() {
        if (selectedCars.size() > 0) {
            showProgress(getString(R.string.dialog_changing_settings), getString(R.string.dialog_wait_content));
            for (Car car : selectedCars) {
                if (!car.isSent()) {
                    changeCarState(car);
                    return;
                }
            }
        }
    }

    private void changeCarState(Car car) {
        clientProvider.getEaClient().setCarState(carsNewState, car.getId());
    }
}