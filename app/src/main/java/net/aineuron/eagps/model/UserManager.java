package net.aineuron.eagps.model;

import com.google.gson.Gson;

import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.transfer.LoginInfo;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vit Veres on 29-May-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class UserManager {

	public static final int WORKER_ID = 2;
	public static final int DISPATCHER_ID = 1;

	public static final Long STATE_ID_READY = 1L;
	public static final Long STATE_ID_BUSY = 2L;
	public static final Long STATE_ID_UNAVAILABLE = 3L;
	public static final Long STATE_ID_BUSY_ORDER = 80L;
	public static final Long STATE_ID_NO_CAR = 90L;
	@Pref
	Pref_ pref;
	@Bean
	ClientProvider clientProvider;
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

	public synchronized User getUser() {
		String userObjectSerialized = pref.userObjectSerialized().get();

		if (userObjectSerialized.isEmpty()) {
			return null;
		}

		return gson.fromJson(userObjectSerialized, User.class);
	}

	public synchronized void setUser(User user) {
		String userObjectSerialized = "";
		if (user != null) {
			userObjectSerialized = gson.toJson(user);
		}
		pref.edit().userObjectSerialized().put(userObjectSerialized).apply();
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
			releaseCar();
		}
		selectState(STATE_ID_NO_CAR);
	}

	public void releaseCar() {
		clientProvider.getEaClient().releaseCar();
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

	public void setToken(String token) {
		clientProvider.getEaClient().setUserToken(token);
	}

	public void login(LoginInfo info) {
		clientProvider.getEaClient().login(info);
	}

	public void logout() {
		clientProvider.getEaClient().logout();
	}
}
