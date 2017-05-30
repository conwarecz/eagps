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

	public static final int WORKER_ID = 1;
	public static final int DISPATCHER_ID = 2;

	public static final int STATE_ID_READY = 1;
	public static final int STATE_ID_BUSY = 2;
	public static final int STATE_ID_UNAVAILABLE = 3;
	public static final int STATE_ID_BUSY_ORDER = 80;
	public static final int STATE_ID_NO_CAR = 90;
	@Pref
	Pref_ pref;
	@Bean
	ClientProvider clientProvider;
	private Map<Integer, String> states = new HashMap<Integer, String>() {
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

	public long getSelectedCarId() {
		return pref.selectedCar().get();
	}

	public void setSelectedCarId(long selectedCarId) {
		pref.edit().selectedCar().put(selectedCarId).apply();
	}

	public void selectCar(long selectedCarId) {
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
		selectState(STATE_ID_NO_CAR);
	}

	public int getSelectedStateId() {
		return pref.selectedState().get();
	}

	public void setSelectedStateId(int stateId) {
		pref.edit().selectedState().put(stateId).apply();
	}

	private void selectState(int stateId) {
		clientProvider.getEaClient().setState(stateId);
	}


	public void login(LoginInfo info) {
		clientProvider.getEaClient().login(info);
	}

	public void logout() {
		clientProvider.getEaClient().logout();
	}
}
