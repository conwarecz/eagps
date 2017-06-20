package net.aineuron.eagps;

import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.event.network.ApiErrorEvent;

import org.androidannotations.annotations.EApplication;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 */

@EApplication
public class Appl extends MultiDexApplication {

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	public static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");

	public static RealmConfiguration dbConfig;

	@EventBusGreenRobot
	EventBus bus;

	@Override
	public void onCreate() {
		super.onCreate();

		initRealm();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onApiErrorEvent(ApiErrorEvent e) {
		Throwable throwable = e.throwable;
		Log.e("EA GPS APP", "API Error: ", throwable);
		String message = "Unknown network error";
		if (throwable.getMessage() != null) {
			message = throwable.getMessage();
		}

		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void initRealm() {
		Realm.init(this);
		dbConfig = new RealmConfiguration.Builder()
				.schemaVersion(1)
				.name("db.realm")
				.deleteRealmIfMigrationNeeded()
				.build();
	}
}
