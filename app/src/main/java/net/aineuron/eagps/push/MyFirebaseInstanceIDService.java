package net.aineuron.eagps.push;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import net.aineuron.eagps.event.network.user.FirebaseTokenRefreshedEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 05.09.2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        if (token != null) {
            EventBus.getDefault().post(new FirebaseTokenRefreshedEvent(token));
        }
    }
}
