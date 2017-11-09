package net.aineuron.eagps.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.aineuron.eagps.Appl;
import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.NewTenderActivity;
import net.aineuron.eagps.activity.NewTenderActivity_;
import net.aineuron.eagps.activity.OrderConfirmationActivity_;
import net.aineuron.eagps.event.network.car.DispatcherRefreshCarsEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.model.TendersManager;
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
import org.androidannotations.annotations.EService;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;

import static net.aineuron.eagps.Appl.NOTIFFICATIONS_CHANNEL_DEFAULT;
import static net.aineuron.eagps.Appl.NOTIFFICATIONS_CHANNEL_TENDER;
import static net.aineuron.eagps.model.UserManager.DISPATCHER_ID;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY_ORDER;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 05.09.2017.
 */

@EService
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final int TENDER_NEW = 1;
    public static final int TENDER_ORDER_UPDATE = 2;
    public static final int TENDER_ORDER_ACCEPTED = 3;
    public static final int TENDER_ORDER_CANCELED = 4;
    public static final int TENDER_NOT_WON = 5;
    public static final int NEW_MESSAGE = 6;
    public static final int CAR_STATUS_CHANGE = 7;
    private static final String TAG = "FCM Service";
    @Bean
    UserManager userManager;
    @Bean
    TendersManager tendersManager;
    //    @Pref
//    Pref_ pref;
    @App
    Appl app;
    private int currentNotificationID = 1;
    private int tenderId = 0;
    private boolean wasInBackground = false;
    private int type = -1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        if(!pref.userObjectSerialized().exists()){
//            return;
//        }
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        app = (Appl) getApplication();
        wasInBackground = app.wasInBackground();
        type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));

        switch (type) {
            case TENDER_NEW:
                handleNewTender(remoteMessage);
                break;
            case TENDER_ORDER_UPDATE:
                handleAcceptedOrder(remoteMessage);
                break;
            case TENDER_ORDER_ACCEPTED:
                handleAcceptedOrder(remoteMessage);
                break;
            case TENDER_ORDER_CANCELED:
                handleCancelledOrder(remoteMessage);
                break;
            case TENDER_NOT_WON:
                handleNotWonTender(remoteMessage);
                break;
            case NEW_MESSAGE:
                handleMessage(remoteMessage);
                break;
            case CAR_STATUS_CHANGE:
                handleCarStatusChange(remoteMessage);
                break;
        }
    }

    private void handleNewTender(RemoteMessage remoteMessage) {
        Tender tender = Tender.getTender(remoteMessage.getData().get("message"));
        Intent notificationIntent = new Intent(this, NewTenderActivity_.class);
        if (tenderId == Integer.MAX_VALUE - 1) {
            tenderId = 0;
        }
        tender.setPushId(tenderId++);
        tender.setIncomeTime(Calendar.getInstance().getTime());
//        OrderSerializable orderSerializable = makeSerializableOrder(tender.getOrder());
//        currentNotificationID = orderSerializable.getId().intValue();
//        notificationIntent.putExtra("orderSerializable", orderSerializable);
//        notificationIntent.putExtra("car", tender.getEntity());
//        notificationIntent.putExtra("incomeDate", Calendar.getInstance().getTime());
        notificationIntent.putExtra("title", remoteMessage.getData().get("title"));
        notificationIntent.putExtra("tenderId", tender.getTenderId());

        boolean hasSameTender = tendersManager.isNextTender(tender.getTenderId());

        tendersManager.addTender(tender);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (wasInBackground || app.getActiveActivity() instanceof NewTenderActivity_) {
            if ((app.getActiveActivity() instanceof NewTenderActivity_ && ((NewTenderActivity) app.getActiveActivity()).getTenderId().equals(tender.getTenderId())) || hasSameTender) {
                return;
            }
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent);
        } else {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), null);
            notificationIntent.putExtra("pushId", currentNotificationID);
            getApplicationContext().startActivity(notificationIntent);
        }
    }

    private void handleAcceptedOrder(RemoteMessage remoteMessage) {
        final Order order = Tender.getOrderFromJson(remoteMessage.getData().get("message"));
        Long id = order.getId();
//        currentNotificationID = id.intValue();
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
        userManager.setStateBusyOnOrder();
//        }
    }

    private void handleMessage(RemoteMessage remoteMessage) {
        final Message message = Tender.getMessageFromJson(remoteMessage.getData().get("message"));
        Long id = message.getId();
//        currentNotificationID = id.intValue();
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
        final Order order = Tender.getOrderFromJson(remoteMessage.getData().get("message"));
        Long id = order.getId();
//        currentNotificationID = id.intValue();
        Realm realm = RealmHelper.getDb();
        realm.executeTransactionAsync(realm1 -> {
            Order canceledOrder = realm.where(Order.class).equalTo("id", id).findFirst();
            canceledOrder.deleteFromRealm();
        });
        Intent notificationIntent = new Intent(this, OrderConfirmationActivity_.class);
        notificationIntent.putExtra("id", id);
        notificationIntent.putExtra("title", remoteMessage.getData().get("title"));
//        if (wasInBackground) {
        sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent);
//        } else {
//            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            getApplicationContext().startActivity(notificationIntent);
        userManager.setStateReady();
        EventBus.getDefault().post(new OrderCanceledEvent(id));
//        }
    }

    private void handleNotWonTender(RemoteMessage remoteMessage) {
        Tender tender = Tender.getTender(remoteMessage.getData().get("message"));
        final Order order = Tender.getOrderFromJson(remoteMessage.getData().get("message"));
        Long id = order.getId();
//        currentNotificationID = id.intValue();
        Intent notificationIntent = new Intent(this, OrderConfirmationActivity_.class);
        notificationIntent.putExtra("id", id);
        notificationIntent.putExtra("title", remoteMessage.getData().get("title"));

        tendersManager.deleteAllOtherTenders(tender.getTenderId());
        sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent);
    }

    private void handleCarStatusChange(RemoteMessage remoteMessage) {
        if (userManager.getUser() == null) {
            return;
        }
        final Long newStatus = Tender.getNewStatusFromJson(remoteMessage.getData().get("message"));
        if (userManager.getUser().getRoleId() == DISPATCHER_ID) {
            EventBus.getDefault().post(new DispatcherRefreshCarsEvent());
            return;
        }
        if (userManager.getSelectedStateId().equals(newStatus)) {
            return;
        } else if (userManager.getSelectedStateId().equals(STATE_ID_BUSY_ORDER) && newStatus.equals(STATE_ID_BUSY)) {
            return;
        } else if (userManager.getSelectedStateId().equals(UserManager.STATE_ID_NO_CAR)) {
            return;
        } else {
            userManager.setSelectedStateId(newStatus);
            EventBus.getDefault().post(new StateSelectedEvent(newStatus));
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), null);
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

        // TODO: předělat na frontu tenderů
        // Tries to remove cancelled tender from notifications
        if (type == TENDER_NOT_WON) {
            notificationManager.cancel(currentNotificationID);
            return;
        }

        currentNotificationID++;

        if (notificationIntent != null) {
            if (type == TENDER_NEW) {
                notificationIntent.putExtra("pushId", currentNotificationID);
            }
        }

        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.ic_launcher);
        NotificationCompat.Builder notificationBuilder = null;
        Notification notification = null;
        if (type == TENDER_NEW) {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            notificationBuilder = new NotificationCompat.Builder(this, NOTIFFICATIONS_CHANNEL_TENDER)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(icon)
                    .setContentTitle(title)
                    .setGroup(String.valueOf(type))
                    .setSound(sound)
                    .setAutoCancel(true)
                    .setContentText(messageBody);

            notification = notificationBuilder.build();
        } else {
            notificationBuilder = new NotificationCompat.Builder(this, NOTIFFICATIONS_CHANNEL_DEFAULT)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(icon)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setGroup(String.valueOf(type))
                    .setContentText(messageBody);

            notification = notificationBuilder.build();
            notification.defaults |= Notification.DEFAULT_SOUND;
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        PendingIntent contentIntent = null;
        if (notificationIntent != null) {
            contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        } else {
            // Only close notification after clicking
            contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
        }

        notificationBuilder.setContentIntent(contentIntent);
        notification = notificationBuilder.build();

//         Ring repeatedly
        if (type == TENDER_NEW && !(app.getActiveActivity() instanceof NewTenderActivity_)) {
            notification.flags |= Notification.FLAG_INSISTENT;
        }

        int notificationId = currentNotificationID;
        if (notificationId == Integer.MAX_VALUE - 1)
            notificationId = 1;

        // Showing only one state change push
        if (type == CAR_STATUS_CHANGE) {
            notificationId = 0;
            notificationManager.cancel(notificationId);
        }

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