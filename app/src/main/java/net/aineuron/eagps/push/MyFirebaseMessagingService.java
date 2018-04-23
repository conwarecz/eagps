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
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.aineuron.eagps.Appl;
import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.NewTenderActivity;
import net.aineuron.eagps.activity.NewTenderActivity_;
import net.aineuron.eagps.activity.OrderConfirmationActivity_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.client.service.EaService;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.event.network.car.DispatcherRefreshCarsEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.order.OrderAcceptedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.event.network.user.UserLoggedOutFromAnotherDeviceEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.TendersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.UserWhoKickedMeFromCar;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.Tender;
import net.aineuron.eagps.model.transfer.KnownError;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

import static net.aineuron.eagps.Appl.NOTIFFICATIONS_CHANNEL_DEFAULT;
import static net.aineuron.eagps.Appl.NOTIFFICATIONS_CHANNEL_TENDER;
import static net.aineuron.eagps.model.UserManager.DISPATCHER_ID;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY_ORDER;
import static net.aineuron.eagps.model.UserManager.STATE_ID_NO_CAR;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 05.09.2017.
 */

@EService
public class MyFirebaseMessagingService extends FirebaseMessagingService {
	// Push Types
	public static final int PUSH_TENDER_NEW = 1;
	public static final int PUSH_ORDER_UPDATED = 2;
	public static final int PUSH_ORDER_ACCEPTED = 3;
	public static final int PUSH_ORDER_CANCELED = 4;
	public static final int PUSH_TENDER_NOT_WON = 5;
	public static final int PUSH_NEW_MESSAGE = 6;
	public static final int PUSH_CAR_STATUS_CHANGE = 7;
	public static final int PUSH_USER_LOGGED_OUT_BY_ANOTHER_USER = 8;
	public static final int PUSH_ORDER_FINISHED = 9;
	public static final int PUSH_ORDER_SENT = 10;
	public static final int PUSH_USER_LOGGED_OUT = 11;
	// Push static ID - to ensure only one notification
	public static final int PUSH_ID_KICKED_FROM_CAR = Integer.MAX_VALUE - 1;
	public static final int PUSH_ID_USER_LOGOUT = Integer.MAX_VALUE - 2;
	public static final int PUSH_ID_USER_STATE_CHANGE = Integer.MAX_VALUE - 3;
	private static final String TAG = "FCM Service";
	@Bean
	UserManager userManager;
	@Bean
	TendersManager tendersManager;
	@Bean
	OrdersManager ordersManager;
	@Bean
	ClientProvider clientProvider;
	@App
	Appl app;
	private boolean isInBackground = false;

	/**
	 * Create and show a simple notification containing the received FCM message.
	 *
	 * @param title       FCM message title received.
	 * @param messageBody FCM message body received.
	 */
	public static void sendNotification(Appl app, int type, int pushId, String title, String messageBody, Intent notificationIntent, boolean intense) {
		NotificationManager notificationManager =
				(NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notificationManager == null) {
			Log.e(TAG, "Notification manager null");
			return;
		}

		Bitmap icon = BitmapFactory.decodeResource(app.getResources(),
				R.mipmap.ic_launcher);
		NotificationCompat.Builder notificationBuilder = null;
		Notification notification = null;
		if (intense) {
			notificationBuilder = new NotificationCompat.Builder(app.getApplicationContext(), NOTIFFICATIONS_CHANNEL_TENDER)
					.setSmallIcon(R.drawable.ic_noti_logo)
					.setLargeIcon(icon)
					.setContentTitle(title)
					.setGroup(String.valueOf(type))
					.setAutoCancel(true)
					.setContentText(messageBody);


			Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			notificationBuilder.setSound(sound);
			notificationBuilder.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400, 100, 200, 300, 400, 500, 400, 300, 200, 400});
			notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
			notificationBuilder.setDefaults(Notification.FLAG_SHOW_LIGHTS);
			notificationBuilder.setLights(0xffff0000, 300, 100);
		} else {
			notificationBuilder = new NotificationCompat.Builder(app.getApplicationContext(), NOTIFFICATIONS_CHANNEL_DEFAULT)
					.setSmallIcon(R.drawable.ic_noti_logo)
					.setLargeIcon(icon)
					.setContentTitle(title)
					.setAutoCancel(true)
					.setGroup(String.valueOf(type))
					.setContentText(messageBody);

			Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			notificationBuilder.setSound(sound);
			notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
			notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
			notificationBuilder.setLights(0xff00ff00, 300, 100);
		}

		PendingIntent contentIntent = null;
		if (notificationIntent != null) {
			contentIntent = PendingIntent.getActivity(app, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
		} else {
			// Only close notification after clicking
			contentIntent = PendingIntent.getActivity(app, 0, new Intent(), 0);
		}

		notificationBuilder.setContentIntent(contentIntent);
		notification = notificationBuilder.build();

		// Ring repeatedly
		if (intense) {
			notification.flags |= Notification.FLAG_INSISTENT;
		}

		try {
			notificationManager.notify(pushId, notification);
		} catch (Exception e) {
			Crashlytics.logException(e);
			e.printStackTrace();
		}
	}

	public static Intent buildNewTenderIntent(Context context, String title) {
		Intent notificationIntent = new Intent(context, NewTenderActivity_.class);
		notificationIntent.putExtra("title", title);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		return notificationIntent;
	}

	public static void cancelNotifications(Context context, List<Integer> pushIds) {
		NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		if (notificationManager == null) {
			Log.e(TAG, "Notification manager null");
			return;
		}

		for (int pushId : pushIds) {
			notificationManager.cancel(pushId);
		}
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		app = (Appl) getApplication();
		isInBackground = app.isInBackground();
		int type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));
		Log.d(TAG, "From: " + remoteMessage.getFrom() + " Not. type:" + type);

		switch (type) {
			case PUSH_TENDER_NEW:
				handleNewTender(remoteMessage);
				break;
			case PUSH_ORDER_UPDATED:
				handleAcceptedOrder(remoteMessage);
				break;
			case PUSH_ORDER_ACCEPTED:
				handleAcceptedOrder(remoteMessage);
				break;
			case PUSH_ORDER_CANCELED:
				handleCancelledOrder(remoteMessage);
				break;
			case PUSH_TENDER_NOT_WON:
				handleNotWonTender(remoteMessage);
				break;
			case PUSH_NEW_MESSAGE:
				handleMessage(remoteMessage);
				break;
			case PUSH_CAR_STATUS_CHANGE:
				handleCarStatusChange(remoteMessage);
				break;
			case PUSH_USER_LOGGED_OUT_BY_ANOTHER_USER:
				handleUserKickedFromCar(remoteMessage);
				break;
			case PUSH_USER_LOGGED_OUT:
				handleUserLoggedOut(remoteMessage);
				break;
		}
	}

	private void handleNewTender(RemoteMessage remoteMessage) {
		int type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));
		Tender tender = Tender.getTender(remoteMessage.getData().get("message"));
		Order order = tender.getOrder();

		boolean hasSameTender = tendersManager.hasTender(tender.getTenderEntityUniId());

		tendersManager.addTender(tender);
		tender = tendersManager.getTenderCopy(tender.getTenderEntityUniId());

		String title = remoteMessage.getData().get("title");
		String body = remoteMessage.getData().get("body");

		Intent notificationIntent = buildNewTenderIntent(this, title);

		if (isInBackground || app.getActiveActivity() instanceof NewTenderActivity_) {
			if (app.getActiveActivity() instanceof NewTenderActivity_ || hasSameTender) {
				return;
			}
			sendNotification(app, type, tender.getPushId(), title, body, notificationIntent, true);
		} else {
			sendNotification(app, type, tender.getPushId(), title, body, null, true);
			getApplicationContext().startActivity(notificationIntent);
		}
	}

	private void handleAcceptedOrder(RemoteMessage remoteMessage) {
		int type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));
		final Tender tender = Tender.getTender(remoteMessage.getData().get("message"));
		final Order order = tender.getOrder();

		Long id = order.getId();

		String title = remoteMessage.getData().get("title");
		String body = remoteMessage.getData().get("body");

		userManager.setSelectedStateId(STATE_ID_BUSY_ORDER);

		boolean intense = false;
		if (userManager.getUser() != null && userManager.getUser().getRoleId() != null) {
			intense = userManager.getUser().getRoleId() == UserManager.WORKER_ID;
		}

		Intent notificationIntent = new Intent(this, OrderConfirmationActivity_.class);
		notificationIntent.putExtra("id", id);
		notificationIntent.putExtra("title", title);
		sendNotification(app, type, id.intValue(), title, body, notificationIntent, intense);

		Realm realm = RealmHelper.getDb();
		realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(order));
		realm.close();

		// Make sure no Tender left on stack from this order
		if (tendersManager.hasTenderByTenderId(tender.getTenderId())) {
			cancelNotifications(this, tendersManager.getPushIdsByTenderId(tender.getTenderId()));

			tendersManager.deleteTendersByTenderId(tender.getTenderId());
			tendersManager.deleteTendersByEntityId(tender.getOrder().getEntityId());
			if (app.getActiveActivity() != null && app.getActiveActivity() instanceof NewTenderActivity_) {
				// When app is in foreground we just show a dialog
				NewTenderActivity newTenderActivity = (NewTenderActivity) app.getActiveActivity();
				new Handler(getMainLooper()).post(() -> newTenderActivity.notWonTender(tender.getTenderEntityUniId(), title));
			}
		}

		EventBus.getDefault().post(new OrderAcceptedEvent(id));
	}

	private void handleMessage(RemoteMessage remoteMessage) {
		int type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));
		final Message message = Tender.getMessageFromJson(remoteMessage.getData().get("message"));
		Long id = message.getId();
		Realm realm = RealmHelper.getDb();
		realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(message));
		Intent notificationIntent = IntentUtils.mainActivityIntent(this, id);
		notificationIntent.putExtra("messageId", id);

		sendNotification(app, type, message.getId().intValue(), remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent, false);
	}

	private void handleCancelledOrder(RemoteMessage remoteMessage) {
		int type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));
		final Order order = Tender.getOrderFromJson(remoteMessage.getData().get("message"));
		Long id = order.getId();
		Realm realm = RealmHelper.getDb();
		realm.executeTransactionAsync(realm1 -> {
			Order canceledOrder = realm.where(Order.class).equalTo("id", id).findFirst();
			canceledOrder.setStatus(Order.ORDER_STATE_CANCELLED);
		});
		realm.close();
		Intent notificationIntent = new Intent(this, OrderConfirmationActivity_.class);
		notificationIntent.putExtra("id", id);
		notificationIntent.putExtra("title", remoteMessage.getData().get("title"));

		sendNotification(app, type, id.intValue(), remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), notificationIntent, false);

		EventBus.getDefault().post(new OrderCanceledEvent(id));
	}

	private void handleNotWonTender(RemoteMessage remoteMessage) {
		int type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));
		Tender tender = Tender.getTender(remoteMessage.getData().get("message"));
		Tender dbCopyTender = tendersManager.getTenderCopy(tender.getTenderEntityUniId());

		if (dbCopyTender == null) {
			// no show notification
			return;
		}

		String title = remoteMessage.getData().get("title");
		String body = remoteMessage.getData().get("body");

		Intent notificationIntent = new Intent(this, OrderConfirmationActivity_.class);
		notificationIntent.putExtra("id", tender.getOrder().getId());
		notificationIntent.putExtra("title", title);

		tendersManager.deleteTender(dbCopyTender.getTenderEntityUniId());

		cancelNotification(dbCopyTender.getPushId());

		if (app.getActiveActivity() != null && app.getActiveActivity() instanceof NewTenderActivity_) {
			// When app is in foreground we just show a dialog
			NewTenderActivity newTenderActivity = (NewTenderActivity) app.getActiveActivity();
			new Handler(getMainLooper()).post(() -> newTenderActivity.notWonTender(dbCopyTender.getTenderEntityUniId(), title));
		} else {
			// If app in background we show notification
			sendNotification(app, type, dbCopyTender.getPushId(), title, body, notificationIntent, false);
		}
	}

	private void handleCarStatusChange(RemoteMessage remoteMessage) {
		EaService service = clientProvider.getEaClient().getEaService();

		User user = userManager.getUser();
		if (user == null) {
			return;
		}

		Tender tender = Tender.getTender(remoteMessage.getData().get("message"));
		if (userManager.getUser().getRoleId() == DISPATCHER_ID) {
			EventBus.getDefault().post(new DispatcherRefreshCarsEvent(tender.getEntityId(), tender.getStatus()));
			return;
		}

		Disposable d = service.getUserEntity(user.getUserId())
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						car -> continueHandleStatusChange(remoteMessage, car),
						Crashlytics::logException
				);
	}

	private void continueHandleStatusChange(RemoteMessage remoteMessage, Car entity) {
		if (entity == null) {
			return;
		}
		int type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));

		Long localStatusId = userManager.getSelectedStateId();
		Long remoteStatusId = entity.getStatusId();
		if (userManager.getSelectedStateId().equals(remoteStatusId)) {
			return;
		} else if (localStatusId.equals(STATE_ID_BUSY_ORDER) && remoteStatusId.equals(STATE_ID_BUSY)) {
			return;
		} else if (localStatusId.equals(STATE_ID_NO_CAR)) {
			return;
		} else {
			userManager.setSelectedStateId(remoteStatusId);
			EventBus.getDefault().post(new StateSelectedEvent(remoteStatusId));
			sendNotification(app, type, PUSH_ID_USER_STATE_CHANGE, remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), null, false);
		}
	}

	private void handleUserKickedFromCar(RemoteMessage remoteMessage) {
		int type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));
		if (userManager.getUser() == null) {
			return;
		}
		final UserWhoKickedMeFromCar user = Tender.getUserWhoKickedMeFromCar(remoteMessage.getData().get("message"));
		KnownError error = new KnownError();
		error.setMessage("Byl jste odhlášen z vozidla uživatelem " + user.getUsername());
		EventBus.getDefault().post(new KnownErrorEvent(error));
		userManager.setSelectedCarId(null);
		userManager.setSelectedStateId(STATE_ID_NO_CAR);
		if (isInBackground) {
			sendNotification(app, type, PUSH_ID_KICKED_FROM_CAR, "Odhlášení z vozidla", "Byl jste odhlášen z vozidla uživatelem " + user.getUsername(), null, false);
		} else {
			EventBus.getDefault().post(new StateSelectedEvent(STATE_ID_NO_CAR));
		}
	}

	private void handleUserLoggedOut(RemoteMessage remoteMessage) {
		int type = Integer.valueOf((remoteMessage.getData().get("notificationtype")));
		if (userManager.getUser() == null) {
			return;
		}

		final UserWhoKickedMeFromCar user = Tender.getUser(remoteMessage.getData().get("message"));
		if (user.getUsername().equalsIgnoreCase(userManager.getUser().getUserName())) {
			if (isInBackground) {
				sendNotification(app, type, PUSH_ID_USER_LOGOUT, "Odhlášení z vozidla", "Byl jste odhlášen", null, false);
			} else {
				EventBus.getDefault().post(new UserLoggedOutFromAnotherDeviceEvent());
			}
			clientProvider.postUnauthorisedError();
		}
	}

	private void cancelNotification(int pushId) {
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (notificationManager == null) {
			Log.e(TAG, "Notification manager null");
			return;
		}

		notificationManager.cancel(pushId);
	}
}