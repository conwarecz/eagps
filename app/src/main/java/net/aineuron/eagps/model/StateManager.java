package net.aineuron.eagps.model;

import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.client.ClientProvider;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vit Veres on 22-May-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class StateManager {

	public static final int STATE_ID_READY = 1;
	public static final int STATE_ID_BUSY = 2;
	public static final int STATE_ID_UNAVAILABLE = 3;
	public static final int STATE_ID_BUSY_ORDER = 80;
	public static final int STATE_ID_NO_CAR = 90;
	@Bean
	ClientProvider clientProvider;
	@Pref
	Pref_ pref;
	private Map<Integer, String> states = new HashMap<Integer, String>() {
		{
			put(STATE_ID_READY, "Ready");
			put(STATE_ID_BUSY, "Busy");
			put(STATE_ID_UNAVAILABLE, "Unavailable");
			put(STATE_ID_BUSY_ORDER, "Busy on order");
			put(STATE_ID_NO_CAR, "No car");
		}
	};

	public String getStateForId(int stateId) {
		if (states.containsKey(stateId)) {
			return states.get(stateId);
		}
		return null;
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
}
