package net.aineuron.eagps;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.event.network.ApiErrorEvent;

import org.androidannotations.annotations.EApplication;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 */

@EApplication
public class Appl extends MultiDexApplication implements
        Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    public static final String NOTIFFICATIONS_CHANNEL_NAME = "default";
    public static String stateOfLifeCycle = "";
    public static boolean wasInBackground = true;
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	public static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
	public static RealmConfiguration dbConfig;
    private static String TAG = Appl.class.getName();
    @EventBusGreenRobot
	EventBus bus;

	@Override
	public void onCreate() {
		super.onCreate();

        registerActivityLifecycleCallbacks(this);
        initRealm();
		initChannels();
        Fabric.with(this, new Crashlytics());
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

	public void initChannels() {
		if (Build.VERSION.SDK_INT < 26) {
			return;
		}
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationChannel channel = new NotificationChannel(NOTIFFICATIONS_CHANNEL_NAME,
				"EA GPS",
				NotificationManager.IMPORTANCE_HIGH);
		channel.setDescription("Notifikace o změnách a zakázkách");
		channel.enableLights(true);
		channel.setLightColor(Color.GREEN);
		channel.enableVibration(true);
		channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
		if (notificationManager != null) {
			notificationManager.createNotificationChannel(channel);
		}
	}

    @Override
    public void onActivityCreated(Activity activity, Bundle arg1) {
        wasInBackground = false;
        stateOfLifeCycle = "Create";
    }

    @Override
    public void onActivityStarted(Activity activity) {
        stateOfLifeCycle = "Start";
    }

    @Override
    public void onActivityResumed(Activity activity) {
        stateOfLifeCycle = "Resume";
        wasInBackground = false;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        stateOfLifeCycle = "Pause";
        wasInBackground = true;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        stateOfLifeCycle = "Stop";
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle arg1) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        wasInBackground = false;
        stateOfLifeCycle = "Destroy";
    }

    @Override
    public void onTrimMemory(int level) {
        if (stateOfLifeCycle.equals("Stop")) {
            wasInBackground = true;
        }
        super.onTrimMemory(level);
    }

    public boolean wasInBackground() {
        return wasInBackground;
    }
}
