package net.aineuron.eagps.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.Appl;
import net.aineuron.eagps.R;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.event.network.order.TenderAcceptSuccessEvent;
import net.aineuron.eagps.event.network.order.TenderRejectSuccessEvent;
import net.aineuron.eagps.event.network.user.UserLoggedOutFromAnotherDeviceEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.TendersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.Location;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.Tender;
import net.aineuron.eagps.model.transfer.KnownError;
import net.aineuron.eagps.model.transfer.PostponedTime;
import net.aineuron.eagps.model.transfer.tender.TenderAcceptModel;
import net.aineuron.eagps.model.transfer.tender.TenderRejectModel;
import net.aineuron.eagps.push.MyFirebaseMessagingService;
import net.aineuron.eagps.util.FormatUtil;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.NetworkUtil;
import net.aineuron.eagps.util.TimeUtil;
import net.aineuron.eagps.view.widget.IcoLabelTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import static net.aineuron.eagps.model.UserManager.DISPATCHER_ID;
import static net.aineuron.eagps.model.UserManager.WORKER_ID;

@EActivity(R.layout.activity_new_tender)
public class NewTenderActivity extends AppCompatActivity {

	private static final String TAG = NewTenderActivity.class.getSimpleName();

	@ColorRes(R.color.highlightAlert)
	int colorHighlightAlert;

	@ViewById(R.id.back)
	Button accept;
	@ViewById(R.id.decline)
	Button decline;
	@Bean
	UserManager userManager;
	@Bean
	OrdersManager ordersManager;
	@Bean
	TendersManager tendersManager;
	@EventBusGreenRobot
	EventBus bus;
	@Bean
	ClientProvider clientProvider;
	@App
	Appl appl;
	@ViewById(R.id.clientCar)
	IcoLabelTextView clientCar;
	@ViewById(R.id.clientAddress)
	IcoLabelTextView clientAddress;
	@ViewById(R.id.destinationAddress)
	IcoLabelTextView destinationAddress;
	@ViewById(R.id.eventDescription)
	IcoLabelTextView eventDescription;
	@ViewById(R.id.postponedArrival)
	IcoLabelTextView postponedArrival;
	@ViewById(R.id.assignedDriver)
	IcoLabelTextView assignedDriver;
	@ViewById(R.id.showOnMap)
	ConstraintLayout map;
	@ViewById(R.id.header)
	TextView header;

	@Extra
	String title;

	PublishSubject<Integer> cancelNotificationsDebounceAction = PublishSubject.create();
	private MaterialDialog progressDialog;
	private Order order;
	private TenderAcceptModel tenderAcceptModel;
	private TenderRejectModel tenderRejectModel;
	private int retryCounter = 0;
	private boolean accepting = false;
	private Tender tender;
	private Long tenderId = -1L;
	private PostponedTime selectedPostponedTime;

	private boolean shouldNotifyNewTender = false;

	@SuppressLint("RestrictedApi")
	@AfterViews
	void afterViews() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null && title != null) {
			setTitle(title);
			actionBar.setTitle(getTitle());
			actionBar.setDefaultDisplayHomeAsUpEnabled(false);
		}
		Disposable subscribe = cancelNotificationsDebounceAction.throttleFirst(4, TimeUnit.SECONDS).subscribe(aInt -> cancelNotifications());
	}

	@Override
	protected void onResume() {
		super.onResume();
		shouldNotifyNewTender = false;
		setUi();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (tender != null) {
				cancelNotificationsDebounceAction.onNext(0);
			}
		}
		// Your code here
		return super.dispatchTouchEvent(ev);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkStateSelectedEvent(StateSelectedEvent e) {
		finishTenderActivity();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onApiErrorEvent(ApiErrorEvent e) {
		new MaterialDialog.Builder(this)
				.content(e.message)
				.positiveText(R.string.confirmation_ok)
				.onPositive((dialog1, which) -> {
					hideProgress();
					dialog1.dismiss();
				})
				.cancelable(false)
				.show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onKnownErrorEvent(KnownErrorEvent e) {
		if (e.knownError.getCode() == 500 && retryCounter < 3) {
			// Retry on 500 errors 3x
			trySendAgain();
			return;
		} else if (retryCounter == 3) {
			retryCounter = 0;
		}

		new MaterialDialog.Builder(this)
				.content(e.knownError.getMessage())
				.positiveText(R.string.confirmation_ok)
				.onPositive((dialog1, which) -> {
					hideProgress();
					dialog1.dismiss();
					handleErrorCode(e.knownError);
				})
				.cancelable(false)
				.show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onOfferCanceledEvent(OrderCanceledEvent e) {
		tendersManager.deleteTendersByTenderId(getTenderId());
		finishTenderActivity();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onTenderAcceptSuccessEvent(TenderAcceptSuccessEvent e) {
		User user = userManager.getUser();
		if (user == null) {
			clientProvider.postUnauthorisedError();
			finishTenderActivity();
			return;
		}

		if (user.getRoleId() == WORKER_ID) {
			tendersManager.deleteAllTenders();
			finishTenderActivity();
		} else {
			tendersManager.deleteTendersByTenderId(getTenderId());
			try {
				tendersManager.deleteTendersByEntityId(tender.getEntity().getId());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			hideProgress();
			setUi();
		}


	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onTenderRejectSuccessEvent(TenderRejectSuccessEvent e) {
		tendersManager.deleteTender(tender.getTenderEntityUniId());
		hideProgress();
		setUi();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onUserLoggedOut(UserLoggedOutFromAnotherDeviceEvent e) {
		Toast.makeText(this, "Byl jste odhlášen", Toast.LENGTH_LONG).show();
	}

	@Click(R.id.back)
	void acceptClicked() {
		if (!NetworkUtil.isConnected(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), R.string.connectivity_not_connected, Toast.LENGTH_LONG).show();
			return;
		}
		accepting = true;

		if (order.isPostponedArrival()) {
			sendAcceptTender();
		} else {
			showPostponedDialog();
		}
	}

	@Click(R.id.decline)
	void declineClicked() {
		if (!NetworkUtil.isConnected(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), R.string.connectivity_not_connected, Toast.LENGTH_LONG).show();
			return;
		}
		accepting = false;
		// State is the same as before
		new MaterialDialog.Builder(this)
				.title("Důvod zrušení")
				.items(R.array.order_reject_choices)
				.itemsIds(R.array.order_reject_choice_ids)
				.autoDismiss(false)
				.itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
					if (which < 0) {
						Toast.makeText(getApplicationContext(), "Vyberte důvod", Toast.LENGTH_SHORT).show();
						return false;
					}
					showProgress(getString(R.string.tender_sending_progress_title), getString(R.string.tender_sending_progress_content));
					tenderRejectModel.setRejectReason((long) which + 1);
					clientProvider.getEaClient().rejectTender(getTenderId(), tenderRejectModel);
					dialog.dismiss();
					return true;
				})
				.positiveText("OK")
				.show();
	}

	@Click(R.id.showOnMap)
	void openMap() {
		if (order.getServiceType() == Order.ORDER_SERVICE_TYPE_VYPROSTENI || order.getServiceType() == Order.ORDER_SERVICE_TYPE_ASISTENCE) {
			IntentUtils.openRoute(this, order.getClientAddress().getLocation(), (Location) null);
			return;
		}
		IntentUtils.openRoute(this, order.getDestinationAddress().getLocation(), order.getClientAddress().getLocation());
	}

	@Click(R.id.clientAddress)
	void openMapClient() {
		IntentUtils.openMapLocation(this, order.getClientAddress().getLocation(), order.getClientFirstName() + " " + order.getClientLastName());
	}

	@Click(R.id.destinationAddress)
	void openMapDestination() {
		IntentUtils.openMapLocation(this, order.getDestinationAddress().getLocation(), order.getWorkshopName());
	}

	public Long getTenderId() {
		return tenderId;
	}

	public void notWonTender(String tenderEntityUniId, String title) {
		if (tender == null) {
			return;
		}

		if (!tender.getTenderEntityUniId().equals(tenderEntityUniId)) {
			return;
		}

		new MaterialDialog.Builder(this)
				.content(title)
				.cancelable(false)
				.positiveText(R.string.confirmation_ok)
				.onPositive((dialog, which) -> {
					setUi();
					dialog.dismiss();
				})
				.show();
	}

	private void setUi() {
		retryCounter = 0;
		tender = tendersManager.getNextTenderCopy();

		if (tender == null) {
			finishTenderActivity();
			return;
		}

		header.setText("Objednávka: " + tender.getOrder().getClaimSaxCode());
		tenderId = tender.getTenderId();
		order = tender.getOrder();

		// Replay notification for new showing
		if (shouldNotifyNewTender) {
			notifyNewTender();
		}
		shouldNotifyNewTender = true;

		User user = userManager.getUser();
		if (user == null) {
			clientProvider.postUnauthorisedError();
			finishTenderActivity();
			return;
		}

		tenderAcceptModel = new TenderAcceptModel();
		if (user.getRoleId() == WORKER_ID && user.getEntity() != null && user.getEntity().getEntityId() != null) {
			tenderAcceptModel.setEntityId(user.getEntity().getEntityId());
		} else if (user.getRoleId() == DISPATCHER_ID && tender.getEntity() != null && tender.getEntity().getId() != null) {
			tenderAcceptModel.setEntityId(tender.getEntity().getId());
		}
		tenderAcceptModel.setUserName(user.getUserName());

		tenderRejectModel = new TenderRejectModel();
		if (user.getRoleId() == WORKER_ID && user.getEntity() != null && user.getEntity().getEntityId() != null) {
			tenderRejectModel.setEntityId(user.getEntity().getEntityId());
		} else if (user.getRoleId() == DISPATCHER_ID && tender.getEntity() != null && tender.getEntity().getId() != null) {
			tenderRejectModel.setEntityId(tender.getEntity().getId());
		}
		tenderRejectModel.setUserName(user.getUserName());


		if (order.getClientCarModel() != null && order.getClientCarWeight() != null && order.getClientCarLicencePlate() != null) {
			this.clientCar.setText(order.getClientCarModel() + ", " + order.getClientCarWeight() + " kg, " + order.getClientCarLicencePlate());
		}

		Address clientAddress = order.getClientAddress();
		if (clientAddress != null) {
			this.clientAddress.setText(FormatUtil.formatClientAddress(clientAddress, order.getClientLocationComment()));
			this.clientAddress.setVisibility(View.VISIBLE);
		} else {
			this.clientAddress.setVisibility(View.GONE);
		}

		Address destinationAddress = order.getDestinationAddress();
		if (destinationAddress != null) {
			this.destinationAddress.setText(FormatUtil.formatDestinationAddress(destinationAddress, order.getWorkshopName()));
			this.destinationAddress.setVisibility(View.VISIBLE);
		} else {
			this.destinationAddress.setVisibility(View.GONE);
		}

		if (clientAddress == null && destinationAddress == null) {
			this.map.setVisibility(View.INVISIBLE);
		} else {
			this.map.setVisibility(View.VISIBLE);
		}

		if (order.getEventDescription() != null) {
			this.eventDescription.setText(FormatUtil.formatEvent(order.getEventDescription()));
		}

		if (order.getArrivalTime() != null) {
			this.postponedArrival.setText(Appl.timeDateFormat.format(order.getArrivalTime()));
		}

		if (order.isPostponedArrival()) {
			this.postponedArrival.setLabelText("Odložený dojezd");
			this.postponedArrival.setTextColor(colorHighlightAlert);
		}

		if (tender.getEntity() != null) {
			String string = "";
			if (tender.getEntity().getName() != null) {
				string = string + tender.getEntity().getName();
			}
			if (tender.getEntity().getAssignedUser() != null) {
				if (!string.isEmpty()) {
					string = string + ", ";
				}
				string = string + tender.getEntity().getAssignedUser();
			}
			this.assignedDriver.setText(string);
		}
	}

	private void handleErrorCode(KnownError knownError) {
		switch (knownError.getCode()) {
			case 500:
				// noop
				return;
			case 4202:
				// Tender has invalid status - 12
				tendersManager.deleteTendersByTenderId(tender.getTenderId());
				break;
			case 4203:
				// Tender entity has invalid status - 13
				tendersManager.deleteTendersByEntityId(tender.getEntity().getId());
				break;
			case 4204:
				// Tender invitation is not valid - 14
				tendersManager.deleteTender(tender.getTenderEntityUniId());
				break;
			case 4207:
				tendersManager.deleteTender(tender.getTenderEntityUniId());
				break;
			case 4209:
				// Too late arrival - 15
				tendersManager.deleteTender(tender.getTenderEntityUniId());
				break;
		}
		setUi();
	}

	private void finishTenderActivity() {
		hideProgress();
		if (!appl.isInBackground()) {
			IntentUtils.openMainActivity(this, true);
		}
		finish();
	}

	private void showProgress(String title, String content) {
		progressDialog = new MaterialDialog.Builder(this)
				.title(title)
				.content(content)
				.cancelable(false)
				.progress(true, 0)
				.show();
	}

	protected void hideProgress() {
		if (progressDialog == null) {
			return;
		}

		if (progressDialog.isCancelled()) {
			return;
		}

		progressDialog.dismiss();
	}

	private void trySendAgain() {
		retryCounter++;
		if (accepting) {
			clientProvider.getEaClient().acceptTender(getTenderId(), tenderAcceptModel);
		} else {
			clientProvider.getEaClient().rejectTender(getTenderId(), tenderRejectModel);
		}
	}

	private void showPostponedDialog() {
		selectedPostponedTime = null;

		Date arrivalTime = order.getArrivalTime();

		List<PostponedTime> postponedTimes = TimeUtil.generatePostponedTimes(arrivalTime, tender.getAllowedDepartureDelayMinutes());
		selectedPostponedTime = postponedTimes.get(0);

		String[] timeNames = new String[postponedTimes.size()];
		for (int i = 0; i < postponedTimes.size(); i++) {
			PostponedTime time = postponedTimes.get(i);
			timeNames[i] = time.getTime();
		}

		final MaterialDialog.Builder builder = new MaterialDialog.Builder(NewTenderActivity.this);
		builder.customView(R.layout.widget_delay_picker, false);
		builder.title(R.string.widget_delay_title);
		final MaterialDialog d = builder.build();
		Button confirmationButton = (AppCompatButton) d.findViewById(R.id.widget_duration_confirm);

		NumberPicker timePicker = (NumberPicker) d.findViewById(R.id.widget_time_picker);
		timePicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		timePicker.setMinValue(0);
		timePicker.setMaxValue(postponedTimes.size() - 1);
		timePicker.setDisplayedValues(timeNames);
		timePicker.setWrapSelectorWheel(false);
		timePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
			this.selectedPostponedTime = postponedTimes.get(newVal);
		});
		d.setOnCancelListener(dialogInterface -> accepting = false);
		confirmationButton.setOnClickListener(v -> {
			d.dismiss();
			tenderAcceptModel.setDepartureDelayMinutes((long) selectedPostponedTime.getOffsetMinutes());
			sendAcceptTender();
		});
		d.show();
	}

	private void sendAcceptTender() {
		showProgress(getString(R.string.tender_sending_progress_title), getString(R.string.tender_sending_progress_content));
		clientProvider.getEaClient().acceptTender(getTenderId(), tenderAcceptModel);
	}

	private void notifyNewTender() {
		Intent intent = MyFirebaseMessagingService.buildNewTenderIntent(this, "Nová objednávka");
		MyFirebaseMessagingService.sendNotification(appl, MyFirebaseMessagingService.PUSH_TENDER_NEW, tender.getPushId(), "Nová objednávka", "Nová objednávka", intent, true);
	}

	private void cancelNotifications() {
		try {
			List<Integer> tenderPushIds = tendersManager.getAllPushIds();
			MyFirebaseMessagingService.cancelNotifications(this, tenderPushIds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
