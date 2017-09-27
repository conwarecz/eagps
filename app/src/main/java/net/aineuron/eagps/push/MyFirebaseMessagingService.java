package net.aineuron.eagps.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.aineuron.eagps.Appl;
import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.NewTenderActivity_;
import net.aineuron.eagps.activity.OrderConfirmationActivity_;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.Tender;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import io.realm.Realm;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 05.09.2017.
 */
@EBean
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final int TENDER_NEW = 1;
    public static final int TENDER_UPDATE = 2;
    public static final int TENDER_ACCEPTED = 3;
    public static final int TENDER_CANCELED = 4;
    public static final int TENDER_NOT_WON = 5;
    public static final int NEW_MESSAGE = 6;
    private static final String TAG = "FCM Service";
    @Bean
    UserManager userManager;
    @App
    Appl app;
    private int currentNotificationID = 0;
    private boolean wasInBackground = false;
    private int type = -1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        app = (Appl) getApplication();
        wasInBackground = app.wasInBackground();
        type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));

        // TODO: předělat logiku pro rozdělení akcí
        switch (type) {
            case TENDER_NEW:
                handleNewTender(remoteMessage);
                break;
            case TENDER_UPDATE:
                handleAcceptedOrder(remoteMessage);
                break;
            case TENDER_ACCEPTED:
                handleAcceptedOrder(remoteMessage);
                break;
            case TENDER_CANCELED:
                handleAcceptedOrder(remoteMessage);
                break;
            case TENDER_NOT_WON:
                handleAcceptedOrder(remoteMessage);
                break;
            case NEW_MESSAGE:
                handleMessage(remoteMessage);
                break;
        }
    }

    private void handleNewTender(RemoteMessage remoteMessage) {
        final Order order = Tender.getOrderFromJson(remoteMessage.getData().get("message"));
        Long id = order.getId();
        Realm realm = RealmHelper.getDb();
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(order));
        Intent notificationIntent = new Intent(this, NewTenderActivity_.class);
        notificationIntent.putExtra("id", id);
        if (wasInBackground) {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent);
        } else {
            getApplicationContext().startActivity(notificationIntent);
        }
    }

    private void handleAcceptedOrder(RemoteMessage remoteMessage) {
        final Order order = Tender.getOrderFromJson(remoteMessage.getData().get("message"));
        Long id = order.getId();
        Realm realm = RealmHelper.getDb();
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(order));
        Intent notificationIntent = new Intent(this, OrderConfirmationActivity_.class);
        notificationIntent.putExtra("id", id);
        notificationIntent.putExtra("title", remoteMessage.getData().get("title"));
        if (wasInBackground) {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent);
        } else {
            getApplicationContext().startActivity(notificationIntent);
        }
    }

    private void handleMessage(RemoteMessage remoteMessage) {
        final Message message = Tender.getMessageFromJson(remoteMessage.getData().get("message"));
        Long id = message.getId();
        Realm realm = RealmHelper.getDb();
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(message));
        Intent notificationIntent = IntentUtils.mainActivityIntent(this, id);
        notificationIntent.putExtra("messageId", id);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        notificationIntent.putExtra("id", id);
//        notificationIntent.putExtra("title", remoteMessage.getData().get("title"));
        if (wasInBackground) {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent);
        } else {
            getApplicationContext().startActivity(notificationIntent);
        }
    }

    private void handleUpdatedOrder(RemoteMessage remoteMessage) {

    }

    private void handleCancelledOrder(RemoteMessage remoteMessage) {

    }

    private void notWonTender(RemoteMessage remoteMessage) {

    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param title       FCM message title received.
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody, Intent notificationIntent) {
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.ic_launcher);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Appl.NOTIFFICATIONS_CHANNEL_NAME)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setCategory(String.valueOf(type))
                .setContentText(messageBody);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(contentIntent);

        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        currentNotificationID++;
        int notificationId = currentNotificationID;
        if (notificationId == Integer.MAX_VALUE - 1)
            notificationId = 0;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notification);
    }
}