package net.aineuron.eagps.client.client;

import android.util.Log;

import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.client.RetrofitException;
import net.aineuron.eagps.client.service.EaService;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.network.car.CarStatusChangedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.event.network.order.OrderSentEvent;
import net.aineuron.eagps.event.network.order.PhotoUploadedEvent;
import net.aineuron.eagps.event.network.order.SheetUploadedEvent;
import net.aineuron.eagps.event.network.user.UserLoggedInEvent;
import net.aineuron.eagps.event.network.user.UserLoggedOutEvent;
import net.aineuron.eagps.event.network.user.UserTokenSet;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Photo;
import net.aineuron.eagps.model.transfer.KnownError;
import net.aineuron.eagps.model.transfer.LoginInfo;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Retrofit;

import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY_ORDER;
import static net.aineuron.eagps.model.UserManager.STATE_ID_NO_CAR;
import static net.aineuron.eagps.model.UserManager.STATE_ID_READY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_UNAVAILABLE;

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
							userManager.setSelectedStateId(car == null ? STATE_ID_NO_CAR : STATE_ID_READY);
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

	public void releaseCar() {
		User user = userManager.getUser();
		if (user == null || user.getUserId() == null || user.getCarId() == null) {
			return;
		}

		eaService.releaseCarFromUser(user.getUserId(), user.getCarId())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						car -> {
							userManager.setSelectedCarId(null);

							user.setCarId(null);
							user.setCar(null);
							userManager.setUser(user);
							userManager.setSelectedStateId(null);
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
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		eaService.getCars(user.getUserId())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						cars -> {
							EventBus.getDefault().post(new CarsDownloadedEvent(cars));
						},
						ClientProvider::postNetworkError
				);
	}

	public void setUserState(Long stateId) {
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		// API resolves only 3 states, but app needs another 2 for internal states
		Long serverState = stateId;

		if (stateId == STATE_ID_BUSY_ORDER) {
			serverState = STATE_ID_BUSY;
		} else if (stateId == STATE_ID_NO_CAR) {
			serverState = STATE_ID_UNAVAILABLE;
		}

		eaService.setStatus(user.getUserId(), serverState)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							userManager.setSelectedStateId(stateId);
							EventBus.getDefault().post(new StateSelectedEvent());
							if (stateId.equals(STATE_ID_NO_CAR)) {
								EventBus.getDefault().post(new CarSelectedEvent());
							}
						},
						ClientProvider::postNetworkError
				);
	}

	public void setUserToken(String token) {
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		eaService.setToken(user.getUserId(), token)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							Log.d("API Token", "Token put to user: " + token);
							user.setToken(token);
							EventBus.getDefault().post(new UserTokenSet());
						},
						ClientProvider::postNetworkError
				);
	}

	public void setCarState(Long stateId, Long carId) {

		eaService.setStatus(carId, stateId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong ->
								EventBus.getDefault().post(new CarStatusChangedEvent(carId))
						,
						ClientProvider::postNetworkError
				);
	}

	// Orders
	public void updateOrders() {
		eaService.getOrders(0, 100)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						orders -> {
							Realm db = RealmHelper.getDb();

							db.executeTransaction(realm -> {
								realm.copyToRealmOrUpdate(orders);
							});

							db.close();
						},
						ClientProvider::postNetworkError);
	}

	public void getOrderDetail(Long orderId) {
		eaService.getOrderDetail(orderId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						order -> {
							Realm db = RealmHelper.getDb();

							db.executeTransaction(realm ->
									realm.copyToRealmOrUpdate(order)
							);

							db.close();
						},
						ClientProvider::postNetworkError
				);
	}

	public void cancelOrder(Long orderId, Long reason) {
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		eaService.cancelOrder(orderId, reason)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong ->
								EventBus.getDefault().post(new OrderCanceledEvent(orderId)),
						ClientProvider::postNetworkError
				);
	}

	public void sendOrder(Long orderId) {
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		eaService.sendOrder(orderId)
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
				.subscribe(
						messages -> {
							Realm db = RealmHelper.getDb();

							db.executeTransaction(realm -> realm.copyToRealmOrUpdate(messages));

							db.close();
						},
						ClientProvider::postNetworkError);
	}

	public void login(LoginInfo info) {
		if (info == null) {
			return;
		}

		eaService.login(info)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						user -> {
							userManager.setUser(user);
							EventBus.getDefault().post(new UserLoggedInEvent());
						}, ClientProvider::postNetworkError);
	}

	public void logout() {
		User loggedUser = userManager.getUser();
		if (loggedUser == null) {
			return;
		}

		eaService.logout(loggedUser.getUserId())
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

	// Pictures
	public void uploadPhoto(Photo photo, Long orderId) {
		eaService.uploadPhoto(orderId, photo)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong ->
								EventBus.getDefault().post(new PhotoUploadedEvent())
						, ClientProvider::postNetworkError
				);
	}

	public void uploadSheet(Photo photo, Long orderId) {
		eaService.uploadSheet(orderId, photo)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong ->
								EventBus.getDefault().post(new SheetUploadedEvent())
						, ClientProvider::postNetworkError
				);
	}
}
