package net.aineuron.eagps.client.client;

import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.client.service.EaService;
import net.aineuron.eagps.event.network.car.CarSelectedEvent;
import net.aineuron.eagps.event.network.car.CarsDownloadedEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.event.network.order.OrderSentEvent;
import net.aineuron.eagps.event.network.user.UserLoggedInEvent;
import net.aineuron.eagps.event.network.user.UserLoggedOutEvent;
import net.aineuron.eagps.model.CarsManager;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.transfer.LoginInfo;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.UUID;
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
	CarsManager carsManager;
	@Bean
	UserManager userManager;
	@Bean
	OrdersManager ordersManager;

	private EaService eaService;

	public EaClient withRetrofit(Retrofit retrofit) {
		this.eaService = retrofit.create(EaService.class);
		return this;
	}

	public void selectCar(Long carId) {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							userManager.setSelectedCarId(carId);
							User user = userManager.getUser();
							Car car = carsManager.getCarById(carId);
							user.setCarId(carId);
							user.setCar(car);
							userManager.setUser(user);
							userManager.setSelectedStateId(car == null ? null : car.getStatusId());
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
		Observable.timer(2, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							Realm db = RealmHelper.getDb();
							Order order = ordersManager.getCurrentOrder();
							order.setId((long) (Math.random() * 1000));
							order.setTime(new Date());

							db.executeTransaction(realm -> realm.copyToRealm(order));

							db.close();
						},
						ClientProvider::postNetworkError
				);
	}

	public void updateMessages() {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						aLong -> {
							Realm db = RealmHelper.getDb();
							Message message = new Message();
							message.setId((long) (Math.random() * 1000));
							message.setMessage(UUID.randomUUID().toString());
							message.setDate(new Date());

							db.executeTransaction(realm -> realm.copyToRealm(message));

							db.close();
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
							userManager.setSelectedCarId(-1l);
							EventBus.getDefault().post(new UserLoggedOutEvent());
						},
						ClientProvider::postNetworkError
				);
	}
}
