package net.aineuron.eagps.client.client;

import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.client.service.EaService;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.user.UserLoggedInEvent;
import net.aineuron.eagps.event.network.user.UserLoggedOutEvent;
import net.aineuron.eagps.model.CarsManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.transfer.LoginInfo;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * Created by Vit Veres on 31.3.2016
 * as a part of AlTraceabilitySystem project.
 */
@EBean(scope = EBean.Scope.Singleton)
public class EaClient {

	@Bean
	CarsManager carsManager;
	@Bean
	UserManager userManager;

	private EaService eaService;

	public EaClient withRetrofit(Retrofit retrofit) {
		this.eaService = retrofit.create(EaService.class);
		return this;
	}

	public void selectCar(long carId) {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							// TODO: Set current state from the selected car
							userManager.setSelectedCarId(carId);
							User user = userManager.getUser();
							user.setCarId(carId);
							userManager.setUser(user);
							EventBus.getDefault().post(new CarSelectedEvent());
						},
						ClientProvider::postNetworkError
				);
	}

	public void getCars() {
		eaService
				.getCars()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						categories -> EventBus.getDefault().post(new CarsDownloadedEvent()),
						ClientProvider::postNetworkError
				);
	}

	public void setState(int stateId) {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							userManager.setSelectedStateId(stateId);
							EventBus.getDefault().post(new StateSelectedEvent());
						},
						ClientProvider::postNetworkError
				);
	}

	public void login(LoginInfo info) {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							// TODO: DO a call "WhoAmI" to get user info
							User user = new User(0, "Jan Novak", "Pracovník", UserManager.WORKER_ID, "+420 123 654 798");
							user.setToken("sdfsdfasdfasdf");
							userManager.setUser(user);
							EventBus.getDefault().post(new UserLoggedInEvent());
						},
						ClientProvider::postNetworkError
				);
	}

	public void logout() {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							userManager.setUser(null);
							userManager.setSelectedCarId(-1);
							EventBus.getDefault().post(new UserLoggedOutEvent());
						},
						ClientProvider::postNetworkError
				);
	}
}
