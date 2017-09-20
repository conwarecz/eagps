package net.aineuron.eagps.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.aineuron.eagps.Appl;
import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase_;
import net.aineuron.eagps.activity.NewOrderActivity_;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.Tender;

import io.reactivex.annotations.Nullable;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 05.09.2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final int TENDER_NEW = 1;
    public static final int TENDER_UPDATE = 2;
    public static final int TENDER_ACCEPTED = 3;
    public static final int TENDER_CANCELED = 4;
    public static final int TENDER_NOT_WON = 5;
    public static final int NEW_MESSAGE = 6;
    private static final String TAG = "FCM Service";
    private int currentNotificationID = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
//        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        Order order = null;
        Message message = null;
        int type = Integer.valueOf(remoteMessage.getData().get("notificationtype"));
        if (type != NEW_MESSAGE) {
            order = Tender.getOrderFromJson(remoteMessage.getData().get("message"));
        } else {
            message = Tender.getMessageFromJson(remoteMessage.getData().get("message"));
        }
        sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), order, message, type);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param title       FCM message title received.
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody, @Nullable Order order, @Nullable Message message, int type) {
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.ic_launcher);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Appl.NOTIFFICATIONS_CHANNEL_NAME)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(messageBody);

        Intent notificationIntent = null;
        Bundle bundle = null;
        if (type == NEW_MESSAGE) {
            // TODO: otvírat detail zprávy - changnout fragment, poslat do něj id a jedeme dálej
            notificationIntent = new Intent(this, MainActivityBase_.class);
            bundle = new Bundle();

            if (message != null) {
                bundle.putSerializable("message", message);
            }
        } else {
            notificationIntent = new Intent(this, NewOrderActivity_.class);
            bundle = new Bundle();
            bundle.putInt("type", type);

            if (order != null) {
                bundle.putSerializable("order", order);
            }
        }

        notificationIntent.putExtras(bundle);

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
