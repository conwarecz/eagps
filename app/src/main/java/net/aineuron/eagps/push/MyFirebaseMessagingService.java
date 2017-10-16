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
import net.aineuron.eagps.model.transfer.tender.AddressDetailSerializable;
import net.aineuron.eagps.model.transfer.tender.AddressSerializable;
import net.aineuron.eagps.model.transfer.tender.LimitationSerializable;
import net.aineuron.eagps.model.transfer.tender.LocationSerializable;
import net.aineuron.eagps.model.transfer.tender.OrderSerializable;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

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
                handleNotWonTender(remoteMessage);
                break;
            case NEW_MESSAGE:
                handleMessage(remoteMessage);
                break;
        }
    }

    private void handleNewTender(RemoteMessage remoteMessage) {
        Tender tender = Tender.getTender(remoteMessage.getData().get("message"));
        Intent notificationIntent = new Intent(this, NewTenderActivity_.class);
        OrderSerializable orderSerializable = makeSerializableOrder(tender.getOrder());
        currentNotificationID = orderSerializable.getId().intValue();
        notificationIntent.putExtra("orderSerializable", orderSerializable);
        notificationIntent.putExtra("title", remoteMessage.getData().get("title"));
        notificationIntent.putExtra("tenderId", tender.getTenderId());
        if (wasInBackground) {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent);
        } else {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            getApplicationContext().startActivity(notificationIntent);
        }
    }

    private void handleAcceptedOrder(RemoteMessage remoteMessage) {
        final Order order = Tender.getOrderFromJson(remoteMessage.getData().get("message"));
        Long id = order.getId();
        currentNotificationID = id.intValue();
        Realm realm = RealmHelper.getDb();
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(order));
        Intent notificationIntent = new Intent(this, OrderConfirmationActivity_.class);
        notificationIntent.putExtra("id", id);
        notificationIntent.putExtra("title", remoteMessage.getData().get("title"));
//        if (wasInBackground) {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent);
//        } else {
//            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            getApplicationContext().startActivity(notificationIntent);
//        }
    }

    private void handleMessage(RemoteMessage remoteMessage) {
        final Message message = Tender.getMessageFromJson(remoteMessage.getData().get("message"));
        Long id = message.getId();
        currentNotificationID = id.intValue();
        Realm realm = RealmHelper.getDb();
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(message));
        Intent notificationIntent = IntentUtils.mainActivityIntent(this, id);
        notificationIntent.putExtra("messageId", id);
//        if (wasInBackground) {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent);
//        } else {
//            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            getApplicationContext().startActivity(notificationIntent);
//        }
    }

    private void handleUpdatedOrder(RemoteMessage remoteMessage) {

    }

    private void handleCancelledOrder(RemoteMessage remoteMessage) {

    }

    private void handleNotWonTender(RemoteMessage remoteMessage) {
        final Order order = Tender.getOrderFromJson(remoteMessage.getData().get("message"));
        Long id = order.getId();
        currentNotificationID = id.intValue();

        // TODO: doděat až bude rozseknuto jak to má vůbec vypadat a chovat se
        if (wasInBackground) {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), null);
        } else {
            Intent notificationIntent = IntentUtils.mainActivityIntent(this, id);
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param title       FCM message title received.
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody, Intent notificationIntent) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tries to remove cancelled tender from notifications
        if (type == TENDER_NOT_WON) {
            notificationManager.cancel(currentNotificationID);
            return;
        }

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

//        currentNotificationID++;
        int notificationId = currentNotificationID;
//        if (notificationId == Integer.MAX_VALUE - 1)
//            notificationId = 0;

        notificationManager.notify(notificationId, notification);
    }

    private OrderSerializable makeSerializableOrder(Order order) {
        OrderSerializable orderSerializable = new OrderSerializable();
        orderSerializable.setStatus(order.getStatus());
        orderSerializable.setClaimSaxCode(order.getClaimSaxCode());

        AddressSerializable clientAddress = null;
        if (order.getClientAddress() != null && order.getClientAddress().getAddress() != null) {
            clientAddress = new AddressSerializable();
            AddressDetailSerializable clientAddressDetail = new AddressDetailSerializable();
            clientAddressDetail.setCity(order.getClientAddress().getAddress().getCity());
            clientAddressDetail.setCountry(order.getClientAddress().getAddress().getCountry());
            clientAddressDetail.setStreet(order.getClientAddress().getAddress().getStreet());
            clientAddressDetail.setZipCode(order.getClientAddress().getAddress().getZipCode());
            clientAddress.setAddress(clientAddressDetail);

            LocationSerializable clientLocation = null;
            if (order.getClientAddress().getLocation() != null) {
                clientLocation = new LocationSerializable();
                clientLocation.setLatitude(order.getClientAddress().getLocation().getLatitude());
                clientLocation.setLongitude(order.getClientAddress().getLocation().getLongitude());
                clientAddress.setLocation(clientLocation);
                orderSerializable.setClientAddress(clientAddress);
            }
        }

        if (order.getDestinationAddress() != null) {
            AddressSerializable destinationAddress = null;
            if (order.getDestinationAddress().getAddress() != null) {
                destinationAddress = new AddressSerializable();
                AddressDetailSerializable destinationAddressDetail = new AddressDetailSerializable();
                destinationAddressDetail.setCity(order.getDestinationAddress().getAddress().getCity());
                destinationAddressDetail.setCountry(order.getDestinationAddress().getAddress().getCountry());
                destinationAddressDetail.setStreet(order.getDestinationAddress().getAddress().getStreet());
                destinationAddressDetail.setZipCode(order.getDestinationAddress().getAddress().getZipCode());
                destinationAddress.setAddress(destinationAddressDetail);
            }

            if (order.getDestinationAddress().getLocation() != null) {
                LocationSerializable destinationLocation = new LocationSerializable();
                destinationLocation.setLatitude(order.getDestinationAddress().getLocation().getLatitude());
                destinationLocation.setLongitude(order.getDestinationAddress().getLocation().getLongitude());
                destinationAddress.setLocation(destinationLocation);
                orderSerializable.setClientAddress(destinationAddress);
            }
        }

        orderSerializable.setDestinationType(order.getDestinationType());
        orderSerializable.setClientCarLicencePlate(order.getClientCarLicencePlate());
        orderSerializable.setClientCarModel(order.getClientCarModel());
        orderSerializable.setClientCarWeight(order.getClientCarWeight());
        List<String> eventDescription = new ArrayList<>();
        for (int i = 0; i < order.getEventDescription().size(); i++) {
            eventDescription.add(order.getEventDescription().get(i).getValue());
        }
        orderSerializable.setEventDescription(eventDescription);
        orderSerializable.setClientFirstName(order.getClientFirstName());
        orderSerializable.setClientLastName(order.getClientLastName());
        orderSerializable.setClientPhone(order.getClientPhone());

        LimitationSerializable limitation = new LimitationSerializable();
        limitation.setLimit(order.getLimitation().getLimit());
        orderSerializable.setLimitation(limitation);

        orderSerializable.setId(order.getId());
        orderSerializable.setWorkshopName(order.getWorkshopName());

        return orderSerializable;
    }
}