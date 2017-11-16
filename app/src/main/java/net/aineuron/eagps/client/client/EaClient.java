package net.aineuron.eagps.client.client;

import android.util.Log;

import com.tmtron.greenannotations.EventBusGreenRobot;

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
import net.aineuron.eagps.model.database.Message;
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
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Response;
import retrofit2.Retrofit;

import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY_ORDER;
import static net.aineuron.eagps.model.UserManager.STATE_ID_NO_CAR;
import static net.aineuron.eagps.model.UserManager.STATE_ID_READY;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_CANCELLED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_FINISHED;

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

	@EventBusGreenRobot
	EventBus eventBus;

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
							eventBus.post(new UserLoggedInEvent());
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
							eventBus.post(new UserLoggedOutEvent());
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
							userManager.setSelectedStateId(car == null ? STATE_ID_NO_CAR : STATE_ID_READY);
							eventBus.post(new CarSelectedEvent());
						},
						this::sendError
				);
	}

	public void releaseCar(Long selectedCarId) {
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
                                    eventBus.post(new CarSelectedEvent());
                                }
                            } else {
                                sendKnownError(voidResponse);
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
								eventBus.post(new UserTokenSet(user.getUserId()));
							} else {
								sendKnownError(voidResponse);
							}
						},
						this::sendError
				);
	}

	public void setCarState(Long stateId, Long carId) {
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
		eaService.getOrders(paging.getSkip(), paging.getTake())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						orders -> {
							if (orders.size() <= 0) {
								eventBus.post(new StopRefreshingEvent());
								return;
							}

							for (Order order : orders) {
								setOrderDatesProperTimeZone(order);
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
		eaService.getOrderDetail(orderId)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						order -> {
							Realm db = RealmHelper.getDb();
							db.executeTransaction(realm ->
									realm.copyToRealmOrUpdate(order)
							);

							setOrderDatesProperTimeZone(order);

							db.close();
							eventBus.post(new StopRefreshingEvent());
						}, this::sendError
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
						{
							if (voidResponse.isSuccessful()) {
								Realm db = RealmHelper.getDb();
								db.executeTransaction(realm ->
										realm.where(Order.class).equalTo("id", orderId).findFirst().setStatus(ORDER_STATE_CANCELLED)
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
									Order order = ordersManager.getOrderById(orderId);
									order.setStatus(ORDER_STATE_FINISHED);
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
		eaService.getMessages(paging.getSkip(), paging.getTake())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						messages -> {
							Realm db = RealmHelper.getDb();

							for (Message message : messages) {
								if (message.getTime() != null) {
									try {
										SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
										simpleDateFormat.setTimeZone(TimeZone.getDefault());
										String string = simpleDateFormat.format(message.getTime());

										simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
										Date newDate = simpleDateFormat.parse(string);
										message.setTime(newDate);
									} catch (ParseException e) {
										e.printStackTrace();
									}
								}
							}

							db.executeTransaction(realm -> realm.copyToRealmOrUpdate(messages));

							db.close();
							eventBus.post(new StopRefreshingEvent());
						},
						this::sendError
				);
	}

	// Pictures
    public void uploadPhoto(PhotoFile photoFile, Long orderId) {
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

	private void sendError(Throwable errorThrowable) {
		try {
			RetrofitException error = (RetrofitException) errorThrowable;

			if (error.getKind() == RetrofitException.Kind.HTTP) {
				KnownError knownError = error.getErrorBodyAs(KnownError.class);
                Log.d("KnownError", knownError.getCode() + knownError.getMessage());
                knownError.setMessage("Požadovaná operace se nezdařila, prosím zkontrolujte své připojení a zkuste to znovu");
                ClientProvider.postKnownError(knownError);
			} else if (error.getKind() == RetrofitException.Kind.UNAUTHORISED) {
				clientProvider.postUnauthorisedError();
			} else {
                Log.d("NetworkError", errorThrowable.getMessage());
                ClientProvider.postNetworkError(errorThrowable, "Požadovaná operace se nezdařila, prosím zkontrolujte své připojení a zkuste to znovu");
            }
		} catch (IOException e) {
			e.printStackTrace();
            Log.d("NetworkError", errorThrowable.getMessage());
            ClientProvider.postNetworkError(errorThrowable, "Požadovaná operace se nezdařila, prosím zkontrolujte své připojení a zkuste to znovu");
        }
		eventBus.post(new StopRefreshingEvent());
	}

	private void sendKnownError(Response<Void> voidResponse) {
		if (voidResponse.code() == 401) {
			clientProvider.postUnauthorisedError();
			return;
		}
		try {
			if (voidResponse.code() == 400) {
				RecognizedError error = RecognizedError.getError(voidResponse.errorBody().string());
				Log.d("KnownError", error.getCode() + error.getMessage());
				KnownError knownError = new KnownError();
				knownError.setCode(error.getCode().intValue());
				knownError.setMessage(error.getMessage());
				ClientProvider.postKnownError(knownError);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
            Log.d("KnownError", voidResponse.code() + voidResponse.errorBody().toString());
            KnownError knownError = new KnownError();
			knownError.setCode(voidResponse.code());
            knownError.setMessage("Požadovaná operace se nezdařila, prosím zkontrolujte své připojení a zkuste to znovu");
            ClientProvider.postKnownError(knownError);
        } catch (Exception e) {
            e.printStackTrace();
		}
		eventBus.post(new StopRefreshingEvent());
	}

	public void acceptTender(Long tenderId, TenderAcceptModel tenderModel) {
		eaService.acceptTender(tenderId, tenderModel)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						voidResponse -> {
							if (voidResponse.isSuccessful()) {
								eventBus.post(new TenderAcceptSuccessEvent());
							} else {
								sendKnownError(voidResponse);
							}
						}
						,
						this::sendError
				);
	}

	public void rejectTender(Long tenderId, TenderRejectModel tenderModel) {
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

	private void setOrderDatesProperTimeZone(Order order) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (order.getEstimatedDepartureTime() != null) {
			try {
				simpleDateFormat.setTimeZone(TimeZone.getDefault());
				String string = simpleDateFormat.format(order.getEstimatedDepartureTime());
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date newDate = simpleDateFormat.parse(string);
				order.setEstimatedDepartureTime(newDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (order.getTimeCreated() != null) {
			try {
				simpleDateFormat.setTimeZone(TimeZone.getDefault());
				String string = simpleDateFormat.format(order.getTimeCreated());
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date newDate = simpleDateFormat.parse(string);
				order.setTimeCreated(newDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
}
