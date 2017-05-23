package net.aineuron.eagps.client.client;

import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.client.service.EaService;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.model.CarsManager;
import net.aineuron.eagps.model.StateManager;

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
	StateManager stateManager;

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
							carsManager.setSelectedCarId(carId);
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
							stateManager.setSelectedStateId(stateId);
							EventBus.getDefault().post(new StateSelectedEvent());
						},
						ClientProvider::postNetworkError
				);
	}
}
