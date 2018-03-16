package net.aineuron.eagps.activity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.MotionEvent;
import android.view.View;
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
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.Tender;
import net.aineuron.eagps.model.transfer.KnownError;
import net.aineuron.eagps.model.transfer.tender.TenderAcceptModel;
import net.aineuron.eagps.model.transfer.tender.TenderRejectModel;
import net.aineuron.eagps.util.FormatUtil;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.NetworkUtil;
import net.aineuron.eagps.view.widget.IcoLabelTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import static net.aineuron.eagps.model.UserManager.DISPATCHER_ID;
import static net.aineuron.eagps.model.UserManager.WORKER_ID;

@EActivity(R.layout.activity_new_tender)
public class NewTenderActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

	private static final String TAG = NewTenderActivity.class.getSimpleName();
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
	@Extra
	String title;
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
	@ViewById(R.id.assignedDriver)
	IcoLabelTextView assignedDriver;
	@ViewById(R.id.showOnMap)
	ConstraintLayout map;
	@ViewById(R.id.header)
	TextView header;
	PublishSubject<Integer> cancelNotificationsDebounceAction = PublishSubject.create();
	private MaterialDialog progressDialog;
	private Order order;
	private TenderAcceptModel tenderAcceptModel;
	private TenderRejectModel tenderRejectModel;
	private int retryCounter = 0;
	private boolean accepting = false;
	private int days = 0;
	private int hours = 0;
	private int minutes = 0;
	private Long duration = 0L;
	private NumberPicker dayPicker;
	private NumberPicker hourPicker;
	private NumberPicker minutePicker;
	private boolean buttonClicked = false;
	private Tender tender;
	private Long tenderId = -1L;

	@SuppressLint("RestrictedApi")
	@AfterViews
	void afterViews() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null && title != null) {
			actionBar.setTitle(title);
			actionBar.setDefaultDisplayHomeAsUpEnabled(false);
			header.setText(title);
		}
		Disposable subscribe = cancelNotificationsDebounceAction.throttleFirst(4, TimeUnit.SECONDS).subscribe(aInt -> cancelNotifications());
	}

	@Override
	protected void onResume() {
		super.onResume();
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
		tendersManager.deleteTendersByTenderId(getTenderId());
		try {
			tendersManager.deleteTendersByEntityId(tender.getEntity().getId());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		hideProgress();
		setUi();
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
		buttonClicked = true;
		accepting = true;
		showDurationDialog();
	}

	@Click(R.id.decline)
	void declineClicked() {
		if (!NetworkUtil.isConnected(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), R.string.connectivity_not_connected, Toast.LENGTH_LONG).show();
			return;
		}
		buttonClicked = true;
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
					tenderRejectModel.setRejectReason((long) which);
					clientProvider.getEaClient().rejectTender(getTenderId(), tenderRejectModel);
					dialog.dismiss();
					return true;
				})
				.positiveText("OK")
				.show();
	}

	@Click(R.id.showOnMap)
	void openMap() {
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

		tenderId = tender.getTenderId();
		order = tender.getOrder();

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
			this.clientAddress.setText(formatClientAddress(clientAddress));
			this.clientAddress.setVisibility(View.VISIBLE);
		} else {
			this.clientAddress.setVisibility(View.GONE);
		}

		Address destinationAddress = order.getDestinationAddress();
		if (destinationAddress != null) {
			this.destinationAddress.setText(formatDestinationAddress(destinationAddress, order.getWorkshopName()));
			this.destinationAddress.setVisibility(View.VISIBLE);
		} else {
			this.destinationAddress.setVisibility(View.GONE);
		}

		if (order.getClientAddress() == null || order.getDestinationAddress() == null) {
			this.map.setVisibility(View.INVISIBLE);
		} else {
			this.map.setVisibility(View.VISIBLE);
		}

		if (order.getEventDescription() != null) {
			this.eventDescription.setText(FormatUtil.formatEvent(order.getEventDescription()));
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

	// Building up addresses from what we have
	@NonNull
	private String formatDestinationAddress(Address destinationAddress, String workshopName) {
		String addressResult = "";
		if (order.getDestinationAddress() != null) {
			if (order.getWorkshopName() != null) {
				addressResult += order.getWorkshopName();
			}
			if (destinationAddress.getAddress().getStreet() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getStreet();
			}
			if (destinationAddress.getAddress().getCity() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getCity();
			}
			if (destinationAddress.getAddress().getZipCode() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getZipCode();
			}
			this.destinationAddress.setText(addressResult);
		}
		return addressResult;
	}

	@NonNull
	private String formatClientAddress(Address clientAddress) {
		String addressResult = "";
		if (order.getClientAddress() != null) {
			if (clientAddress.getAddress().getStreet() != null) {
				addressResult += clientAddress.getAddress().getStreet();
			}
			if (clientAddress.getAddress().getCity() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += clientAddress.getAddress().getCity();
			}
			if (clientAddress.getAddress().getZipCode() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += clientAddress.getAddress().getZipCode();
			}
			this.clientAddress.setText(addressResult);
		}
		return addressResult;
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


	private void showDurationDialog() {
		duration = 0L;
		days = 0;
		hours = 0;
		minutes = 0;
		final MaterialDialog.Builder builder = new MaterialDialog.Builder(NewTenderActivity.this);
		builder.customView(R.layout.widget_delay_picker, false);
		builder.title(R.string.widget_delay_title);
		final MaterialDialog d = builder.build();
		Button confirmationButton = (AppCompatButton) d.findViewById(R.id.widget_duration_confirm);
		dayPicker = (NumberPicker) d.findViewById(R.id.widget_duration_days);
		dayPicker.setMaxValue(99);
		dayPicker.setMinValue(0);
		dayPicker.setWrapSelectorWheel(false);
		dayPicker.setOnValueChangedListener(this);

		hourPicker = (NumberPicker) d.findViewById(R.id.widget_duration_hours);
		hourPicker.setMaxValue(23);
		hourPicker.setMinValue(0);
		hourPicker.setWrapSelectorWheel(false);
		hourPicker.setOnValueChangedListener(this);

		minutePicker = (NumberPicker) d.findViewById(R.id.widget_duration_minutes);
		minutePicker.setMaxValue(11);
		minutePicker.setMinValue(0);
		minutePicker.setDisplayedValues(new String[]{"0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"});
		minutePicker.setWrapSelectorWheel(false);
		minutePicker.setOnValueChangedListener(this);
		d.setOnCancelListener(dialogInterface -> accepting = false);
		confirmationButton.setOnClickListener(v -> {
			d.dismiss();
			tenderAcceptModel.setDepartureDelayMinutes(duration);
			showProgress(getString(R.string.tender_sending_progress_title), getString(R.string.tender_sending_progress_content));
			clientProvider.getEaClient().acceptTender(getTenderId(), tenderAcceptModel);
		});
		d.show();
	}

	@Override
	public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
		if (numberPicker == dayPicker) {
			days = newValue;
		} else if (numberPicker == hourPicker) {
			hours = newValue;
		} else if (numberPicker == minutePicker) {
			minutes = newValue * 5;
		}
		duration = Long.valueOf(minutes + (hours * 60) + (days * 60 * 24));
	}

	private void cancelNotifications() {
		try {
			List<Integer> tenderPushIds = tendersManager.getAllPushIds();
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			for (int id : tenderPushIds) {
				notificationManager.cancel(id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
