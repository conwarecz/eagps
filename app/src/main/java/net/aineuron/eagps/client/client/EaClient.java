package net.aineuron.eagps.client.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.BuildConfig;
import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.client.RetrofitException;
import net.aineuron.eagps.client.service.EaService;
import net.aineuron.eagps.event.network.car.CarReleasedEvent;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.network.car.CarStatusChangedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.event.network.order.OrderFinalizedEvent;
import net.aineuron.eagps.event.network.order.OrderSentEvent;
import net.aineuron.eagps.event.network.order.PhotoUploadedEvent;
import net.aineuron.eagps.event.network.order.SheetUploadedEvent;
import net.aineuron.eagps.event.network.order.TenderAcceptSuccessEvent;
import net.aineuron.eagps.event.network.order.TenderRejectSuccessEvent;
import net.aineuron.eagps.event.network.user.UserDataGotEvent;
import net.aineuron.eagps.event.network.user.UserLoggedInEvent;
import net.aineuron.eagps.event.network.user.UserLoggedOutEvent;
import net.aineuron.eagps.event.network.user.UserTokenSet;
import net.aineuron.eagps.event.ui.StopRefreshingEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.Entity;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.PhotoFile;
import net.aineuron.eagps.model.database.order.Reasons;
import net.aineuron.eagps.model.database.order.ReasonsRequestBody;
import net.aineuron.eagps.model.transfer.KnownError;
import net.aineuron.eagps.model.transfer.LoginInfo;
import net.aineuron.eagps.model.transfer.Paging;
import net.aineuron.eagps.model.transfer.RecognizedError;
import net.aineuron.eagps.model.transfer.tender.TenderAcceptModel;
import net.aineuron.eagps.model.transfer.tender.TenderRejectModel;
import net.aineuron.eagps.util.NetworkUtil;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Response;
import retrofit2.Retrofit;

import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY_ORDER;
import static net.aineuron.eagps.model.UserManager.STATE_ID_NO_CAR;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_CANCELLED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_FINISHED;

/**
 * Created by Vit Veres on 31.3.2016
 * as a part of AlTraceabilitySystem project.
 */
@SuppressLint("CheckResult")
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

	@RootContext
	Context context;

	@EventBusGreenRobot
	EventBus eventBus;

	private EaService eaService;

	public EaClient withRetrofit(Retrofit retrofit) {
		this.eaService = retrofit.create(EaService.class);
		return this;
	}

	public EaService getEaService() {
		return eaService;
	}

	public void login(LoginInfo info) {
		if (!connectedToInternet()) {
			return;
		}
		if (info == null) {
			return;
		}

		eaService.login(getClearVersionName(), info)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						userResponse -> {
							if (userResponse.isSuccessful()) {
								userManager.setUser(userResponse.body());
								clientProvider.rebuildRetrofit();
								eventBus.post(new UserLoggedInEvent());
							} else {
								sendKnownError(userResponse);
							}
						},
						this::sendError
				);
	}

	public void logout(User user) {
		if (!connectedToInternet()) {
			return;
		}
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
							eventBus.post(new UserLoggedOutEvent());
						},
						this::sendError
				);
	}

	public void getUser(Long userId) {
		if (!connectedToInternet()) {
			return;
		}
		// TODO: Consider adding call to get user details, to avoid copying state from previous login info
		eaService.getUser(userId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						user -> {
							if (userManager.getUser() == null) {
								// Currently not logged user
								clientProvider.postUnauthorisedError();
								return;
							}

							// Need to keep some user info stored in User after login response
							user.setUserId(user.getId());
							user.setToken(userManager.getUser().getToken());
							user.setUserName(userManager.getUser().getUserName());
							if (userManager.getUser().getRoleId() != null && userManager.getUser().getRoleId().intValue() > 0) {
								user.setRoleId(userManager.getUser().getRoleId());
								user.setUserRole(userManager.getUser().getRoleId());
							} else if (user.getUserRole() != null && user.getUserRole().intValue() > 0) {
								user.setRoleId(user.getUserRole());
							}

							userManager.setUser(user);

							// Dispatcher doesn't have Entity
							if (user.getEntity() != null && user.getEntity().getEntityStatus() != null) {
								userManager.setSelectedStateId(user.getEntity().getEntityStatus());
							} else {
								userManager.setSelectedStateId(STATE_ID_NO_CAR);
							}
							if (user.getEntity() != null && user.getEntity().getEntityId() != null) {
								userManager.setSelectedCarId(user.getEntity().getEntityId());
							}

							// Copy all active orders to DB
							if (user.getCurrentOrders() != null && user.getCurrentOrders().size() > 0) {
								userManager.setStateBusyOnOrder();
								Realm db = RealmHelper.getDb();
								db.executeTransaction(realm -> {
									for (Order order : user.getCurrentOrders()) {
										realm.copyToRealmOrUpdate(order);
									}
								});
								db.close();
							}
							eventBus.post(new UserDataGotEvent());
						},
						this::sendError
				);
	}

	public void selectCar(Long carId) {
		if (!connectedToInternet()) {
			return;
		}
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

							Entity entity = new Entity();
							entity.setEntityId(car.getId());
							entity.setEntityStatus(car.getStatusId());
							entity.setLicencePlate(car.getLicencePlate());
							user.setEntity(entity);

							user.setCarId(carId);
							user.setCar(car);
							userManager.setUser(user);
							userManager.setSelectedStateId(car.getStatusId());
							eventBus.post(new CarSelectedEvent(car.getStatusId()));
						},
						this::sendError
				);
	}

	public void releaseCar(Long selectedCarId) {
		if (!connectedToInternet()) {
			return;
		}
		User user = userManager.getUser();
		if (user == null || user.getUserId() == null || user.getEntity() == null || user.getEntity().getEntityId() == null) {
			return;
		}

		eaService.releaseCarFromUser(user.getUserId(), user.getEntity().getEntityId())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						car -> {
							userManager.setSelectedCarId(null);

							user.setCarId(null);
							user.setCar(null);
							user.setEntity(null);
							userManager.setUser(user);
							userManager.setSelectedStateId(null);
							eventBus.post(new CarReleasedEvent(selectedCarId));
						},
						this::sendError
				);
	}

	public void getCars() {
		if (!connectedToInternet()) {
			return;
		}
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		eaService.getCars(user.getUserId())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						cars -> {
							eventBus.post(new CarsDownloadedEvent(cars));
						},
						this::sendError
				);
	}

	public void setUserState(Long stateId) {
		if (!connectedToInternet()) {
			return;
		}
		if (stateId.equals(STATE_ID_BUSY_ORDER)) {
			return;
		}
		User user = userManager.getUser();
		if (user == null || user.getEntity() == null || user.getEntity().getEntityId() == null) {
			return;
		}

		// API resolves only 3 states, but app needs another 2 for internal states
		Long serverState = stateId;

		if (stateId == STATE_ID_BUSY_ORDER) {
			serverState = STATE_ID_BUSY;
		}

		eaService.setStatus(user.getEntity().getEntityId(), serverState)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
								userManager.setSelectedStateId(stateId);
								eventBus.post(new StateSelectedEvent(stateId));
								if (stateId.equals(STATE_ID_NO_CAR)) {
									eventBus.post(new CarSelectedEvent(stateId));
								}
							} else {
								sendKnownError(voidResponse);
							}
						},
						this::sendError
				);
	}

	public void setUserFirebaseToken(String token) {
		if (!connectedToInternet()) {
			return;
		}
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
								pref.tokenFcm().put(token);
								eventBus.post(new UserTokenSet(user.getUserId()));
							} else {
								sendKnownError(voidResponse);
							}
						},
						this::sendError
				);
	}

	public void setCarState(Long stateId, Long carId) {
		if (!connectedToInternet()) {
			return;
		}
		if (stateId.equals(STATE_ID_BUSY_ORDER)) {
			return;
		}
		eaService.setStatus(carId, stateId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse ->
						{
							if (voidResponse.isSuccessful()) {
								eventBus.post(new CarStatusChangedEvent(carId));
							} else {
								sendKnownError(voidResponse);
							}
						}
						,
						this::sendError
				);
	}

	// Orders
	public void updateOrders(Paging paging) {
		if (!connectedToInternet()) {
			return;
		}
		eaService.getOrders(paging.getSkip(), paging.getTake())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						orders -> {
							if (orders.size() <= 0) {
								eventBus.post(new StopRefreshingEvent());
								return;
							}

							Realm db = RealmHelper.getDb();
							db.executeTransaction(realm ->
									realm.copyToRealmOrUpdate(orders)
							);

							db.close();
							eventBus.post(new StopRefreshingEvent());
						},
						this::sendError
				);
	}

	public void getOrderDetail(Long orderId) {
		if (!connectedToInternet()) {
			return;
		}
		eaService.getOrderDetail(orderId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						orderResponse -> {
							Order order = orderResponse.body();
							if (order != null) {
								Realm db = RealmHelper.getDb();
								db.executeTransaction(realm ->
										realm.copyToRealmOrUpdate(order)
								);


								db.close();
							}

							eventBus.post(new StopRefreshingEvent());
						}, this::sendError
				);
	}

	public void cancelOrder(Long orderId, Long reason) {
		if (!connectedToInternet()) {
			return;
		}
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		eaService.cancelOrder(orderId, reason)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						(Response<Void> voidResponse) ->
						{
							if (voidResponse.isSuccessful()) {
								Realm db = RealmHelper.getDb();
								db.executeTransaction(realm -> {
											Order order = realm.where(Order.class).equalTo("id", orderId).findFirst();
											if (order != null) {
												order.setStatus(ORDER_STATE_CANCELLED);
											}
										}
								);
								db.close();
								eventBus.post(new OrderCanceledEvent(orderId));
							} else {
								sendKnownError(voidResponse);
							}
						}
						, this::sendError
				);
	}

	public void finalizeOrder(Long orderId) {
		if (!connectedToInternet()) {
			return;
		}
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		eaService.finalizeOrder(orderId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
								Realm db = RealmHelper.getDb();
								db.executeTransaction(realm -> {
									Order order = realm.where(Order.class).equalTo("id", orderId).findFirst();
									if (order != null) {
										order.setStatus(ORDER_STATE_FINISHED);
									}
								});
								db.close();
								eventBus.post(new OrderFinalizedEvent(orderId));
							} else {
								sendKnownError(voidResponse);
							}
						},
						this::sendError
				);
	}

	public void sendOrder(Long orderId, Reasons reasons) {
		if (!connectedToInternet()) {
			return;
		}
		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		ReasonsRequestBody body = new ReasonsRequestBody();
		body.setReasonForNoDocuments(reasons.getReasonForNoDocuments());
		body.setReasonForNoPhotos(reasons.getReasonForNoPhotos());

		eaService.sendOrder(orderId, body)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
//								if (userManager.haveActiveOrder()) {
//									userManager.setSelectedStateId(STATE_ID_BUSY_ORDER);
//									eventBus.post(new StateSelectedEvent(STATE_ID_BUSY_ORDER));
//								} else {
//									userManager.setSelectedStateId(STATE_ID_READY);
//									eventBus.post(new StateSelectedEvent(STATE_ID_READY));
//								}
								eventBus.post(new OrderSentEvent(orderId));
							} else {
								sendKnownError(voidResponse);
							}
						},
						this::sendError
				);
	}

	public void setMessageRead(Long messageId, boolean isRead) {
		if (!connectedToInternet()) {
			return;
		}
		if (messageId == null) {
			return;
		}

		eaService.setRead(messageId, isRead)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
								Log.d("MessageSetRead", "Message Set Read success");
							} else {
								sendKnownError(voidResponse);
							}
						},
						this::sendError
				);
	}

	public void updateMessages(Paging paging) {
		if (!connectedToInternet()) {
			return;
		}
		eaService.getMessages(paging.getSkip(), paging.getTake())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						messages -> {
							Realm db = RealmHelper.getDb();
							db.executeTransaction(realm -> realm.copyToRealmOrUpdate(messages));
							db.close();

							eventBus.post(new StopRefreshingEvent());
						},
						this::sendError
				);
	}

	// Pictures
	public void uploadPhoto(PhotoFile photoFile, Long orderId) {
		if (!connectedToInternet()) {
			return;
		}
		eaService.uploadPhoto(orderId, photoFile)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse ->
						{
							if (voidResponse.isSuccessful()) {
								eventBus.post(new PhotoUploadedEvent());
							} else {
								sendKnownError(voidResponse);
							}
						}
						, this::sendError
				);
	}

	public void uploadSheet(PhotoFile photoFile, Long orderId) {
		if (!connectedToInternet()) {
			return;
		}
		eaService.uploadSheet(orderId, photoFile)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
								eventBus.post(new SheetUploadedEvent());
							} else {
								sendKnownError(voidResponse);
							}
						}
						,
						this::sendError
				);
	}

	public void acceptTender(Long tenderId, TenderAcceptModel tenderModel) {
		if (!connectedToInternet()) {
			return;
		}
		eaService.acceptTender(tenderId, tenderModel)
				.flatMap(voidResponse -> {
					if (voidResponse.isSuccessful()) {
						return eaService.getOrderDetail(tenderId);
					} else {
						return Maybe.just(voidResponse);
					}
				})
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						response -> {
							if (response.isSuccessful()) {
								if (response.body() instanceof Order) {
									// Check whether response came from accept tender or from getOrderDetail
									Order order = (Order) response.body();
									if (order != null) {
										Realm db = RealmHelper.getDb();
										db.executeTransaction(realm ->
												realm.copyToRealmOrUpdate(order)
										);


										db.close();
										userManager.setSelectedStateId(STATE_ID_BUSY_ORDER);
									}

								}

								eventBus.post(new TenderAcceptSuccessEvent());
							} else {
								sendKnownError(response);
							}
						}
						,
						this::sendError
				);
	}

	public void rejectTender(Long tenderId, TenderRejectModel tenderModel) {
		if (!connectedToInternet()) {
			return;
		}
		eaService.rejectTender(tenderId, tenderModel)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
								eventBus.post(new TenderRejectSuccessEvent());
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

			if (error.getKind() == RetrofitException.Kind.UNAUTHORISED) {
				clientProvider.postUnauthorisedError();
			} else if (error.getResponse().code() == 400) {
				try {
					RecognizedError recognizedError = RecognizedError.getError(error.getResponse().errorBody().string());
					Log.d("KnownError", recognizedError.getCode() + recognizedError.getMessage());
					KnownError knownError = new KnownError();
					knownError.setCode(recognizedError.getCode().intValue());
					knownError.setMessage(recognizedError.getMessage());
					ClientProvider.postKnownError(knownError);
					return;
				} catch (Exception e) {
					Crashlytics.logException(e);
					e.printStackTrace();
				}
			} else if (error.getResponse().code() == 403) {
				RecognizedError recognizedError = new RecognizedError();
				try {
					recognizedError = RecognizedError.getError(error.getResponse().errorBody().string());
					Log.d("KnownError", recognizedError.getCode() + recognizedError.getMessage());
				} catch (Exception e) {
					Crashlytics.logException(e);
					e.printStackTrace();
				}
				KnownError knownError = new KnownError();
				knownError.setCode(403);
				knownError.setMessage(recognizedError.getMessage());
				ClientProvider.postKnownError(knownError);
				return;
			}

			Crashlytics.logException(error);
			if (error.getKind() == RetrofitException.Kind.HTTP) {
				KnownError knownError = error.getErrorBodyAs(KnownError.class);
				Log.d("KnownError", knownError.getCode() + knownError.getMessage());
				knownError.setMessage("Požadovaná operace se nezdařila, prosím zkontrolujte své připojení a zkuste to znovu");
				ClientProvider.postKnownError(knownError);
			} else {
				Log.d("NetworkError", errorThrowable.getMessage());
				ClientProvider.postNetworkError(errorThrowable, "Požadovaná operace se nezdařila, prosím zkontrolujte své připojení a zkuste to znovu");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Crashlytics.logException(e);
			Log.d("NetworkError", errorThrowable.getMessage());
			ClientProvider.postNetworkError(errorThrowable, "Požadovaná operace se nezdařila, prosím zkontrolujte své připojení a zkuste to znovu");
		}
		eventBus.post(new StopRefreshingEvent());
	}

	private void sendKnownError(Response response) {
		int code = response.code();
		if (code == 401) {
			clientProvider.postUnauthorisedError();
			return;
		}
		try {
			if (code == 400) {
				RecognizedError error = RecognizedError.getError(response.errorBody().string());
				Log.d("KnownError", error.getCode() + error.getMessage());
				KnownError knownError = new KnownError();
				knownError.setCode(error.getCode().intValue());
				knownError.setMessage(error.getMessage());
				ClientProvider.postKnownError(knownError);
				return;
			} else if (code == 403) {
				RecognizedError recognizedError = new RecognizedError();
				try {
					recognizedError = RecognizedError.getError(response.errorBody().string());
					Log.d("KnownError", recognizedError.getCode() + recognizedError.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					Crashlytics.logException(e);
				}
				KnownError knownError = new KnownError();
				knownError.setCode(403);
				knownError.setMessage(recognizedError.getMessage());
				ClientProvider.postKnownError(knownError);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Crashlytics.logException(e);
		}
		try {
			Log.d("KnownError", code + response.errorBody().toString());
			KnownError knownError = new KnownError();
			knownError.setCode(code);
			knownError.setMessage("Požadovaná operace se nezdařila, prosím zkontrolujte své připojení a zkuste to znovu");
			ClientProvider.postKnownError(knownError);
		} catch (Exception e) {
			e.printStackTrace();
			Crashlytics.logException(e);
		}
		eventBus.post(new StopRefreshingEvent());
	}

	private boolean connectedToInternet() {
		boolean connected = true;
		if (!NetworkUtil.isConnected(context)) {
			KnownError knownError = new KnownError();
			knownError.setCode(400);
			knownError.setMessage("Nejste připojen k internetu");
			ClientProvider.postKnownError(knownError);
			connected = false;
		}
		return connected;
	}

	private String getClearVersionName() {
		String versionName = BuildConfig.VERSION_NAME;
		try {
			versionName = versionName.substring(0, versionName.indexOf("-"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public void testErrorCode() {
		eaService.testErrorResponse(4301L)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
								Log.d("Error test", "Succeeded, whaaaat?");
							} else {
								sendKnownError(voidResponse);
							}
						}
						,
						this::sendError
				);
	}
}
