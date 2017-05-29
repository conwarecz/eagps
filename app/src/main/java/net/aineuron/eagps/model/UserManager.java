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

/**
 * Created by Vit Veres on 29-May-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class UserManager {

	public static final int WORKER_ID = 1;
	public static final int DISPATCHER_ID = 2;

	@Pref
	Pref_ pref;

	@Bean
	ClientProvider clientProvider;

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

	public void login(LoginInfo info) {
		clientProvider.getEaClient().login(info);
	}

	public void logout() {
		clientProvider.getEaClient().logout();
	}
}
