package net.aineuron.eagps.activity;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
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
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.database.RealmString;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.AddressDetail;
import net.aineuron.eagps.model.database.order.Limitation;
import net.aineuron.eagps.model.database.order.Location;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.transfer.tender.OrderSerializable;
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

import io.realm.RealmList;

import static net.aineuron.eagps.model.UserManager.DISPATCHER_ID;
import static net.aineuron.eagps.model.UserManager.WORKER_ID;

@EActivity(R.layout.activity_new_tender)
public class NewTenderActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

	public static boolean isVisible = false;
	@ViewById(R.id.back)
	Button accept;
	@ViewById(R.id.decline)
	Button decline;
	@Bean
	UserManager userManager;
	@Bean
	OrdersManager ordersManager;
	@EventBusGreenRobot
	EventBus bus;
	@Bean
	ClientProvider clientProvider;
	@Extra
	OrderSerializable orderSerializable;
	@Extra
	String title;
	@Extra
	Long tenderId;
	@Extra
	Car car;
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
	private MaterialDialog progressDialog;
	private Order order;
    private TenderAcceptModel tenderAcceptModel;
    private TenderRejectModel tenderRejectModel;
	private int retryCounter = 0;
	private boolean accepting = true;
	private int days = 0;
	private int hours = 0;
	private int minutes = 0;
	private Long duration = 0L;
	private NumberPicker dayPicker;
	private NumberPicker hourPicker;
	private NumberPicker minutePicker;
	private boolean buttonClicked = false;

	@AfterViews
	void afterViews() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null && title != null) {
			actionBar.setTitle(title);
			actionBar.setDefaultDisplayHomeAsUpEnabled(false);
			header.setText(title);
		}

		if (orderSerializable != null) {
			makeRealmOrder();
		}

		User user = userManager.getUser();

        tenderAcceptModel = new TenderAcceptModel();
		if (user.getRoleId() == WORKER_ID && user.getEntity() != null && user.getEntity().getEntityId() != null) {
			tenderAcceptModel.setEntityId(user.getEntity().getEntityId());
		} else if (user.getRoleId() == DISPATCHER_ID && car != null && car.getId() != null) {
			tenderAcceptModel.setEntityId(car.getId());
		}
		tenderAcceptModel.setUserName(user.getUserName());

        tenderRejectModel = new TenderRejectModel();
		if (user.getRoleId() == WORKER_ID && user.getEntity() != null && user.getEntity().getEntityId() != null) {
			tenderRejectModel.setEntityId(user.getEntity().getEntityId());
		} else if (user.getRoleId() == DISPATCHER_ID && car != null && car.getId() != null) {
			tenderRejectModel.setEntityId(car.getId());
		}
		tenderRejectModel.setUserName(user.getUserName());


		setUi();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isVisible = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		isVisible = false;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkStateSelectedEvent(StateSelectedEvent e) {
        finishTenderActivity();
    }

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkStateSelectedEvent(ApiErrorEvent e) {
		Toast.makeText(getApplicationContext(), e.throwable.getMessage(), Toast.LENGTH_SHORT).show();
        finishTenderActivity();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onKnownErrorEvent(KnownErrorEvent e) {
		if (retryCounter < 3) {
			trySendAgain();
			Toast.makeText(getApplicationContext(), "Pokus " + (retryCounter + 1) + ": " + e.knownError.getMessage(), Toast.LENGTH_SHORT).show();
		} else {
			finishTenderActivity();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onOfferCanceledEvent(OrderCanceledEvent e) {
		finishTenderActivity();
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTenderAcceptSuccessEvent(TenderAcceptSuccessEvent e) {
        finishTenderActivity();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTenderRejectSuccessEvent(TenderRejectSuccessEvent e) {
        finishTenderActivity();
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
				.items(R.array.order_cancel_choices)
				.itemsIds(R.array.order_cancel_choice_ids)
				.itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
					if (which < 0) {
						Toast.makeText(getApplicationContext(), "Vyberte důvod", Toast.LENGTH_SHORT).show();
						return false;
					}
					showProgress(getString(R.string.tender_sending_progress_title), getString(R.string.tender_sending_progress_content));
					tenderRejectModel.setRejectReason(Long.valueOf(which));
					clientProvider.getEaClient().rejectTender(tenderId, tenderRejectModel);
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

	private void setUi() {
		if (order.getClientCarModel() != null && order.getClientCarWeight() != null && order.getClientCarLicencePlate() != null) {
			this.clientCar.setText(order.getClientCarModel() + ", " + order.getClientCarWeight() + ", " + order.getClientCarLicencePlate());
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

		if (car != null) {
			String string = "";
			if (car.getName() != null) {
				string = string + car.getName();
			}
			if (car.getAssignedUser() != null) {
				if (!string.isEmpty()) {
					string = string + ", ";
				}
				string = string + car.getAssignedUser();
			}
			this.assignedDriver.setText(string);
		}
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
        if (!appl.wasInBackground()) {
            IntentUtils.openMainActivity(this);
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

	private void makeRealmOrder() {
		order = new Order();
		order.setStatus(orderSerializable.getStatus());
		order.setClaimSaxCode(orderSerializable.getClaimSaxCode());

		if (orderSerializable.getClientAddress() != null) {
			Address clientAddress = new Address();
			if (orderSerializable.getClientAddress().getAddress() != null) {
				AddressDetail clientAddressDetail = new AddressDetail();
				clientAddressDetail.setCity(orderSerializable.getClientAddress().getAddress().getCity());
				clientAddressDetail.setCountry(orderSerializable.getClientAddress().getAddress().getCountry());
				clientAddressDetail.setStreet(orderSerializable.getClientAddress().getAddress().getStreet());
				clientAddressDetail.setZipCode(orderSerializable.getClientAddress().getAddress().getZipCode());
				clientAddress.setAddress(clientAddressDetail);
			}

			if (orderSerializable.getClientAddress().getLocation() != null) {
				Location clientLocation = new Location();
				clientLocation.setLatitude(orderSerializable.getClientAddress().getLocation().getLatitude());
				clientLocation.setLongitude(orderSerializable.getClientAddress().getLocation().getLongitude());
				clientAddress.setLocation(clientLocation);
			}
			order.setClientAddress(clientAddress);
		}

		if (orderSerializable.getDestinationAddress() != null) {
			Address destinationAddress = new Address();
			if (orderSerializable.getDestinationAddress().getAddress() != null) {
				AddressDetail destinationAddressDetail = new AddressDetail();
				destinationAddressDetail.setCity(orderSerializable.getDestinationAddress().getAddress().getCity());
				destinationAddressDetail.setCountry(orderSerializable.getDestinationAddress().getAddress().getCountry());
				destinationAddressDetail.setStreet(orderSerializable.getDestinationAddress().getAddress().getStreet());
				destinationAddressDetail.setZipCode(orderSerializable.getDestinationAddress().getAddress().getZipCode());
				destinationAddress.setAddress(destinationAddressDetail);
			}

			if (orderSerializable.getDestinationAddress().getLocation() != null) {
				Location destinationLocation = new Location();
				destinationLocation.setLatitude(orderSerializable.getDestinationAddress().getLocation().getLatitude());
				destinationLocation.setLongitude(orderSerializable.getDestinationAddress().getLocation().getLongitude());
				destinationAddress.setLocation(destinationLocation);
			}
			order.setClientAddress(destinationAddress);
		}

		order.setDestinationType(orderSerializable.getDestinationType());
		order.setClientCarLicencePlate(orderSerializable.getClientCarLicencePlate());
		order.setClientCarModel(orderSerializable.getClientCarModel());
		order.setClientCarWeight(orderSerializable.getClientCarWeight());
		RealmList<RealmString> eventDescription = new RealmList<>();
		for (int i = 0; i < orderSerializable.getEventDescription().size(); i++) {
			RealmString event = new RealmString();
			event.setValue(orderSerializable.getEventDescription().get(i));
			eventDescription.add(event);
		}
		order.setEventDescription(eventDescription);
		order.setClientFirstName(orderSerializable.getClientFirstName());
		order.setClientLastName(orderSerializable.getClientLastName());
		order.setClientPhone(orderSerializable.getClientPhone());

		Limitation limitation = new Limitation();
		limitation.setLimit(orderSerializable.getLimitation().getLimit());
		order.setLimitation(limitation);

		order.setId(orderSerializable.getId());
		order.setWorkshopName(orderSerializable.getWorkshopName());
	}

	private void trySendAgain() {
		retryCounter++;
		if (accepting) {
			clientProvider.getEaClient().acceptTender(tenderId, tenderAcceptModel);
		} else {
			clientProvider.getEaClient().rejectTender(tenderId, tenderRejectModel);
		}
	}


	public void showDurationDialog() {
		final Dialog d = new Dialog(NewTenderActivity.this);
		d.setTitle("Vyberte zpoždění výjezdu");
		d.setContentView(R.layout.widget_delay_picker);
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
		confirmationButton.setOnClickListener(v -> {
			d.dismiss();
			tenderAcceptModel.setDepartureDelayMinutes(duration);
			showProgress(getString(R.string.tender_sending_progress_title), getString(R.string.tender_sending_progress_content));
			clientProvider.getEaClient().acceptTender(tenderId, tenderAcceptModel);
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

	public boolean isButtonClicked() {
		return buttonClicked;
	}
}
