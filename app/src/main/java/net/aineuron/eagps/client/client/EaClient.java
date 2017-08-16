package net.aineuron.eagps.client.client;

import android.util.Log;

import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.client.RetrofitException;
import net.aineuron.eagps.client.service.EaService;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.event.network.order.OrderSentEvent;
import net.aineuron.eagps.event.network.user.UserLoggedInEvent;
import net.aineuron.eagps.event.network.user.UserLoggedOutEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.transfer.KnownError;
import net.aineuron.eagps.model.transfer.LoginInfo;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Retrofit;

/**
 * Created by Vit Veres on 31.3.2016
 * as a part of AlTraceabilitySystem project.
 */
@EBean(scope = EBean.Scope.Singleton)
public class EaClient {

	@Bean
	UserManager userManager;
	@Bean
	OrdersManager ordersManager;
	@Bean
	ClientProvider clientProvider;

	private EaService eaService;

	public EaClient withRetrofit(Retrofit retrofit) {
		this.eaService = retrofit.create(EaService.class);
		return this;
	}

	public void selectCar(Long carId) {
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		eaService.setCarToUser(user.getUserId(), carId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						car -> {
							userManager.setSelectedCarId(carId);

							user.setCarId(carId);
							user.setCar(car);
							userManager.setUser(user);
							userManager.setSelectedStateId(car == null ? null : car.getStatusId());
							EventBus.getDefault().post(new CarSelectedEvent());
						},
						errorThrowable -> {
							RetrofitException error = (RetrofitException) errorThrowable;

							if (error.getKind() == RetrofitException.Kind.HTTP) {
								KnownError knownError = error.getErrorBodyAs(KnownError.class);
								ClientProvider.postKnownError(knownError);
							} else {
								ClientProvider.postNetworkError(errorThrowable);
							}
						}
				);
	}

	public void getCars() {
		eaService
				.getCars(1L)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						cars -> {
							EventBus.getDefault().post(new CarsDownloadedEvent(cars));
						},
						ClientProvider::postNetworkError
				);
	}

	public void setState(Long stateId) {
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

	public void cancelOrder(Long orderId) {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							EventBus.getDefault().post(new OrderCanceledEvent(orderId));
						},
						ClientProvider::postNetworkError
				);
	}

	public void sendOrder(Long orderId) {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							userManager.setSelectedStateId(UserManager.STATE_ID_READY);
							EventBus.getDefault().post(new OrderSentEvent(orderId));
						},
						ClientProvider::postNetworkError
				);
	}

	public void updateOrders() {
		eaService.getOrders(0, 100)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(orders -> {
							Realm db = RealmHelper.getDb();

							db.executeTransaction(realm -> {
								realm.copyToRealmOrUpdate(orders);
							});

							db.close();
						},
						ClientProvider::postNetworkError);
	}

	public void setMessageRead(Long messageId, boolean isRead) {
		if (messageId == null) {
			return;
		}

		eaService.setRead(messageId, isRead)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(aVoid -> Log.d("MessageSetRead", "Message Set Read success"),
						ClientProvider::postNetworkError);
	}

	public void updateMessages() {
		eaService.getMessages(0, 100)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(messages -> {
							Realm db = RealmHelper.getDb();

							db.executeTransaction(realm -> realm.copyToRealmOrUpdate(messages));

							db.close();
						},
						ClientProvider::postNetworkError);
	}

	public void login(LoginInfo info) {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							// TODO: DO a call "WhoAmI" to get user info
							User user = new User(1, "Jan Novak", "Řidič", UserManager.WORKER_ID, "+420 123 654 798");
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
							userManager.setSelectedCarId(-1l);
							EventBus.getDefault().post(new UserLoggedOutEvent());
						},
						ClientProvider::postNetworkError
				);
	}
}
