package net.aineuron.eagps.client.client;

import android.util.Log;

import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.client.RetrofitException;
import net.aineuron.eagps.client.service.EaService;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.network.car.CarStatusChangedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.event.network.order.OrderFinalizedEvent;
import net.aineuron.eagps.event.network.order.OrderSentEvent;
import net.aineuron.eagps.event.network.order.PhotoUploadedEvent;
import net.aineuron.eagps.event.network.order.SheetUploadedEvent;
import net.aineuron.eagps.event.network.user.UserDataGotEvent;
import net.aineuron.eagps.event.network.user.UserLoggedInEvent;
import net.aineuron.eagps.event.network.user.UserLoggedOutEvent;
import net.aineuron.eagps.event.network.user.UserTokenSet;
import net.aineuron.eagps.event.ui.StopRefreshingEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Photo;
import net.aineuron.eagps.model.transfer.KnownError;
import net.aineuron.eagps.model.transfer.LoginInfo;
import net.aineuron.eagps.model.transfer.Paging;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Response;
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
	@Pref
	Pref_ pref;

	private EaService eaService;

	public EaClient withRetrofit(Retrofit retrofit) {
		this.eaService = retrofit.create(EaService.class);
		return this;
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
							clientProvider.rebuildRetrofit();
							EventBus.getDefault().post(new UserLoggedInEvent());
						},
						this::sendError
				);
	}

	public void logout(User user) {
		if (user == null) {
			return;
		}

		eaService.logout(user.getUserId())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							userManager.setUser(null);
							userManager.setSelectedCarId(-1l);
							EventBus.getDefault().post(new UserLoggedOutEvent());
						},
						this::sendError
				);
	}

	public void getUser(Long userId) {
		eaService.getUser(userId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						user -> {
							// Need to keep some user info stored in User afted login response
							user.setUserId(user.getId());
							user.setToken(userManager.getUser().getToken());
							user.setUserName(userManager.getUser().getUserName());
							user.setRoleId(userManager.getUser().getRoleId());
							user.setUserRole(userManager.getUser().getRoleId());

							userManager.setUser(user);

							// Dispatcher doesn't have Entity
							if (user.getEntity() != null && user.getEntity().getEntityStatus() != null) {
								userManager.setSelectedStateId(user.getEntity().getEntityStatus());
							}
							if (user.getEntity() != null && user.getEntity().getEntityId() != null) {
								userManager.setSelectedCarId(user.getEntity().getEntityId());
							}

							// Copy all active orders to DB
//							if(user.getCurrentOrders() != null && user.getCurrentOrders().size() > 0){
//								userManager.setStateBusyOnOrder();
//								Realm db = RealmHelper.getDb();
//								db.executeTransaction(realm -> {
//									for(Order order : user.getCurrentOrders()){
//										realm.copyToRealmOrUpdate(order);
//									}
//								});
//								db.close();
//							}
							EventBus.getDefault().post(new UserDataGotEvent());
						},
						this::sendError
				);
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
						this::sendError
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
						this::sendError
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
						this::sendError
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

		eaService.setStatus(user.getEntity().getEntityId(), serverState)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							userManager.setSelectedStateId(stateId);
							EventBus.getDefault().post(new StateSelectedEvent());
							if (stateId.equals(STATE_ID_NO_CAR)) {
								EventBus.getDefault().post(new CarSelectedEvent());
							}
						},
						this::sendError
				);
	}

	public void setUserFirebaseToken(String token) {
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		eaService.setToken(user.getUserId(), token)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
								Log.d("FCM Token", "Token put to user: " + token);
								EventBus.getDefault().post(new UserTokenSet(user.getUserId()));
							} else {
								sendKnownError(voidResponse);
							}
						},
						this::sendError
				);
	}

	public void setCarState(Long stateId, Long carId) {
		eaService.setStatus(carId, stateId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse ->
								EventBus.getDefault().post(new CarStatusChangedEvent(carId))
						,
						this::sendError
				);
	}

	// Orders
	public void updateOrders(Paging paging) {
		eaService.getOrders(paging.getSkip(), paging.getTake())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						orders -> {
							Realm db = RealmHelper.getDb();

							db.executeTransaction(realm -> {
								realm.copyToRealmOrUpdate(orders);
							});

							db.close();
							EventBus.getDefault().post(new StopRefreshingEvent());
						},
						this::sendError
				);
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
						this::sendError
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
						voidResponse ->
								EventBus.getDefault().post(new OrderCanceledEvent(orderId)),
						this::sendError
				);
	}

	public void finalizeOrder(Long orderId) {
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		eaService.finalizeOrder(orderId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							userManager.setSelectedStateId(UserManager.STATE_ID_READY);
							EventBus.getDefault().post(new OrderFinalizedEvent(orderId));
						},
						this::sendError
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
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
								if (userManager.haveActiveOrder()) {
									userManager.setSelectedStateId(UserManager.STATE_ID_BUSY_ORDER);
								} else {
									userManager.setSelectedStateId(UserManager.STATE_ID_READY);
								}
								EventBus.getDefault().post(new OrderSentEvent(orderId));
							} else {
								sendKnownError(voidResponse);
							}
						},
						this::sendError
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
						this::sendError
				);
	}

	public void updateMessages(Paging paging) {
		eaService.getMessages(paging.getSkip(), paging.getTake())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						messages -> {
							Realm db = RealmHelper.getDb();

							db.executeTransaction(realm -> realm.copyToRealmOrUpdate(messages));

							db.close();
							EventBus.getDefault().post(new StopRefreshingEvent());
						},
						this::sendError
				);
	}

	// Pictures
	public void uploadPhoto(Photo photo, Long orderId) {
		eaService.uploadPhoto(orderId, photo)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse ->
								EventBus.getDefault().post(new PhotoUploadedEvent())
						,
						this::sendError
				);
	}

	public void uploadSheet(Photo photo, Long orderId) {
		eaService.uploadSheet(orderId, photo)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
								EventBus.getDefault().post(new SheetUploadedEvent());
							} else {
								sendKnownError(voidResponse);
							}
						}
						,
						this::sendError
				);
	}

	private void sendError(Throwable errorThrowable) {
		try {
			RetrofitException error = (RetrofitException) errorThrowable;

			if (error.getKind() == RetrofitException.Kind.HTTP) {
				KnownError knownError = error.getErrorBodyAs(KnownError.class);
				ClientProvider.postKnownError(knownError);
			} else if (error.getKind() == RetrofitException.Kind.UNAUTHORISED) {
				clientProvider.postUnauthorisedError();
			} else {
				ClientProvider.postNetworkError(errorThrowable);
			}
		} catch (IOException e) {
			e.printStackTrace();
			ClientProvider.postNetworkError(errorThrowable);
		}
		EventBus.getDefault().post(new StopRefreshingEvent());
	}

	private void sendKnownError(Response<Void> voidResponse) {
		if (voidResponse.code() == 401) {
			clientProvider.postUnauthorisedError();
			return;
		}
		try {
			KnownError knownError = new KnownError();
			knownError.setCode(voidResponse.code());
			knownError.setMessage(voidResponse.errorBody().string());
			ClientProvider.postKnownError(knownError);
		} catch (IOException e) {
			e.printStackTrace();
		}
		EventBus.getDefault().post(new StopRefreshingEvent());
	}
}
