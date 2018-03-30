package net.aineuron.eagps;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
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
import java.util.TimeZone;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static android.app.Notification.VISIBILITY_PUBLIC;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 */

@EApplication
public class Appl extends MultiDexApplication implements
        Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    public static final String NOTIFFICATIONS_CHANNEL_DEFAULT = "notification_channel_default";
    public static final String NOTIFFICATIONS_CHANNEL_TENDER = "notification_channel_tender";
    public static final String NOTIFFICATIONS_CHANNEL_DEFAULT_NAME = "DEFAULT NOTIFICATION";
    public static final String NOTIFFICATIONS_CHANNEL_TENDER_NAME = "TENDER NOTIFICATION";
    public static String stateOfLifeCycle = "";
    public static boolean isInBackground = true;
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    public static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat timeDateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    public static RealmConfiguration dbConfig;
    public static RealmConfiguration tenderDbConfig;
    private static String TAG = Appl.class.getName();
    @EventBusGreenRobot
	EventBus bus;
    private Activity activeActivity;

	@Override
	public void onCreate() {
		super.onCreate();

        dateFormat.setTimeZone(TimeZone.getDefault());
        timeFormat.setTimeZone(TimeZone.getDefault());

        initChannels();
        registerActivityLifecycleCallbacks(this);
        initRealm();
        Fabric.with(this, new Crashlytics());
    }

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onApiErrorEvent(ApiErrorEvent e) {
		Throwable throwable = e.throwable;
		Log.e("EA GPS APP", "API Error: ", throwable);

        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show();
    }

	private void initRealm() {
		Realm.init(this);
        dbConfig = new RealmConfiguration.Builder()
                .schemaVersion(1)
                .name("db.realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        tenderDbConfig = new RealmConfiguration.Builder()
                .schemaVersion(1)
                .name("tenders.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
    }

	public void initChannels() {
		if (Build.VERSION.SDK_INT < 26) {
			return;
		}
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(NOTIFFICATIONS_CHANNEL_DEFAULT,
                NOTIFFICATIONS_CHANNEL_DEFAULT_NAME,
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Notifikace o změnách a zakázkách");
        channel.enableLights(true);
        channel.setLightColor(Color.GREEN);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(VISIBILITY_PUBLIC);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), channel.getAudioAttributes());

        NotificationChannel tenderChannel = new NotificationChannel(NOTIFFICATIONS_CHANNEL_TENDER,
                NOTIFFICATIONS_CHANNEL_TENDER_NAME,
                NotificationManager.IMPORTANCE_HIGH);
        tenderChannel.setDescription("Notifikace o nových soutěžích");
        tenderChannel.enableLights(true);
        tenderChannel.setLightColor(Color.RED);
        tenderChannel.enableVibration(true);
        tenderChannel.setBypassDnd(true);
        tenderChannel.setLockscreenVisibility(VISIBILITY_PUBLIC);
        tenderChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400});
        tenderChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), tenderChannel.getAudioAttributes());

		if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(tenderChannel);
        }
	}

    @Override
    public void onActivityCreated(Activity activity, Bundle arg1) {
        isInBackground = false;
        stateOfLifeCycle = "Create";
    }

    @Override
    public void onActivityStarted(Activity activity) {
        stateOfLifeCycle = "Start";
    }

    @Override
    public void onActivityResumed(Activity activity) {
        stateOfLifeCycle = "Resume";
        isInBackground = false;
        activeActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        stateOfLifeCycle = "Pause";
        isInBackground = true;
        activeActivity = null;
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
        isInBackground = false;
        stateOfLifeCycle = "Destroy";
    }

    @Override
    public void onTrimMemory(int level) {
        if (stateOfLifeCycle.equals("Stop")) {
            isInBackground = true;
        }
        super.onTrimMemory(level);
    }

    public boolean isInBackground() {
        return isInBackground;
    }

    public Activity getActiveActivity() {
        return activeActivity;
    }
}
