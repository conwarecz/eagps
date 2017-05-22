package net.aineuron.eagps.client.client;

import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.client.service.EaService;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;

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
public class EaClient {
	private final Pref_ pref;
	private EaService eaService;

	public EaClient(Retrofit retrofit, Pref_ pref) {
		this.eaService = retrofit.create(EaService.class);
		this.pref = pref;
	}

	public void selectCar(long carId) {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> EventBus.getDefault().post(new CarSelectedEvent()),
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

	public void setState(String state) {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> EventBus.getDefault().post(new StateSelectedEvent()),
						ClientProvider::postNetworkError

				);
	}
}
