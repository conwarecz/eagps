package net.aineuron.eagps.model;

import android.app.NotificationManager;
import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import net.aineuron.eagps.Appl;
import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.transfer.LoginInfo;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_ARRIVED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_ASSIGNED;

/**
 * Created by Vit Veres on 29-May-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class UserManager {

	public static final int WORKER_ID = 2;
	public static final int DISPATCHER_ID = 1;

    public static final Long STATE_ID_UNAVAILABLE = 1L;
    public static final Long STATE_ID_READY = 2L;
    public static final Long STATE_ID_BUSY = 3L;
    public static final Long STATE_ID_BUSY_ORDER = 80L;
	public static final Long STATE_ID_NO_CAR = 90L;
	@Pref
	Pref_ pref;
	@App
	Appl app;
	@Bean
	ClientProvider clientProvider;
    @Bean
    OrdersManager ordersManager;
    private Map<Long, String> states = new HashMap<Long, String>() {
		{
			put(STATE_ID_READY, "Ready");
			put(STATE_ID_BUSY, "Busy");
			put(STATE_ID_UNAVAILABLE, "Unavailable");
			put(STATE_ID_BUSY_ORDER, "Busy on order");
			put(STATE_ID_NO_CAR, "No car");
		}
	};
	private Gson gson;

	@AfterInject
	public void afterInject() {
		gson = new Gson();
	}

    public User getUser() {
        String userObjectSerialized = pref.userObjectSerialized().get();

		if (userObjectSerialized.isEmpty()) {
			return null;
		}

		return gson.fromJson(userObjectSerialized, User.class);
	}

    public void setUser(User user) {
        String userObjectSerialized = "";
		if (user != null) {
			userObjectSerialized = gson.toJson(user);
			pref.token().put(user.getToken());
		}
        pref.userObjectSerialized().put(userObjectSerialized);
    }

	public Long getSelectedCarId() {
		Long value = pref.selectedCar().get();
		if (value == -1) {
			return null;
		}
		return value;
	}

	public void setSelectedCarId(Long selectedCarId) {
		long value = -1;
		if (selectedCarId != null) {
			value = selectedCarId;
		}

		pref.edit().selectedCar().put(value).apply();
	}


	public void selectCar(Long selectedCarId) {
		clientProvider.getEaClient().selectCar(selectedCarId);
	}

	public void setStateReady() {
		selectState(STATE_ID_READY);
	}

	public void setStateBusy() {
		selectState(STATE_ID_BUSY);
	}

	public void setStateUnavailable() {
		selectState(STATE_ID_UNAVAILABLE);
	}

	public void setStateBusyOnOrder() {
		selectState(STATE_ID_BUSY_ORDER);
	}

	public void setStateNoCar() {
		if (!getSelectedStateId().equals(STATE_ID_NO_CAR)) {
            releaseCar(null);
        }
        setSelectedStateId(STATE_ID_NO_CAR);
        EventBus.getDefault().post(new StateSelectedEvent(STATE_ID_NO_CAR));
    }

    public void releaseCar(Long selectedCarId) {
        clientProvider.getEaClient().releaseCar(selectedCarId);
    }

	public Long getSelectedStateId() {
		Long value = pref.selectedState().get();
		if (value == null) {
			return STATE_ID_NO_CAR;
		}
		return value;
	}

	public void setSelectedStateId(Long stateId) {
		int value = STATE_ID_NO_CAR.intValue();
		if (stateId != null) {
			value = stateId.intValue();
		}
		pref.edit().selectedState().put(value).apply();
	}

	private void selectState(Long stateId) {
        clientProvider.getEaClient().setUserState(stateId);
    }

	public void setFirebaseToken(String token) {
        if (pref.token().get() != null && !pref.token().get().isEmpty()) {
            clientProvider.rebuildRetrofit();
        }
        clientProvider.getEaClient().setUserFirebaseToken(token);
	}

	public void login(LoginInfo info) {
		clientProvider.getEaClient().login(info);
	}

	@Background
	public void logout(User user) {
		deleteUser();
		if (user != null) {
			clientProvider.getEaClient().logout(user);
		}
	}

	public void deleteUser() {
		final String token = FirebaseInstanceId.getInstance().getToken();
		try {
			FirebaseInstanceId.getInstance().deleteToken(token, token);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			FirebaseInstanceId.getInstance().deleteInstanceId();
		} catch (IOException e) {
			e.printStackTrace();
		}
		pref.clear();
		ordersManager.clearDatabase();
		try {
			NotificationManager notificationManager =
					(NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean haveActiveOrder() {
		boolean activeOrder = false;
		Realm db = RealmHelper.getDb();
		RealmResults<Order> activeOrders = db.where(Order.class)
				.beginGroup()
				.equalTo("status", ORDER_STATE_ASSIGNED)
				.or()
				.equalTo("status", ORDER_STATE_ARRIVED)
				.endGroup()
				.findAll();
		if (activeOrders.size() > 0) {
			activeOrder = true;
            setSelectedStateId(STATE_ID_BUSY_ORDER);
        }
        return activeOrder;
    }

    public void getUserData(Long userId) {
        clientProvider.getEaClient().getUser(userId);
    }
}
