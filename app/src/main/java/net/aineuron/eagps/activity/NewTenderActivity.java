package net.aineuron.eagps.activity;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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
import net.aineuron.eagps.model.database.RealmString;
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
import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

@EActivity(R.layout.activity_offer)
public class NewTenderActivity extends AppCompatActivity {

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
	@ViewById(R.id.showOnMap)
	ConstraintLayout map;

	private MaterialDialog progressDialog;
	private Order order;
    private TenderAcceptModel tenderAcceptModel;
    private TenderRejectModel tenderRejectModel;

	@AfterViews
	void afterViews() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null && title != null) {
			actionBar.setTitle(title);
			actionBar.setDefaultDisplayHomeAsUpEnabled(false);
		}

		if (orderSerializable != null) {
			makeRealmOrder();
		}

        tenderAcceptModel = new TenderAcceptModel();
        tenderAcceptModel.setEntityId(userManager.getUser().getEntity().getEntityId());
        tenderAcceptModel.setUserName(userManager.getUser().getUserName());

        tenderRejectModel = new TenderRejectModel();
        tenderRejectModel.setEntityId(userManager.getUser().getEntity().getEntityId());
        tenderRejectModel.setUserName(userManager.getUser().getUserName());

		setUi();
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
        Toast.makeText(getApplicationContext(), e.knownError.getMessage(), Toast.LENGTH_SHORT).show();
        finishTenderActivity();
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
        TimeDurationPickerDialog dialog = new TimeDurationPickerDialog(this, new TimeDurationPickerDialog.OnDurationSetListener() {
            @Override
            public void onDurationSet(TimeDurationPicker view, long duration) {
                long minutes = duration / 1000 / 60;
                tenderAcceptModel.setDepartureDelayMinutes(minutes);
                clientProvider.getEaClient().acceptTender(tenderId, tenderAcceptModel);
            }
        }, 0L, TimeDurationPicker.HH_MM);
        dialog.setTitle("Vyberte zpoždění výjezdu");
        dialog.show();
    }

	@Click(R.id.decline)
	void declineClicked() {
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
//                    showProgress("Odesílám zamítnutí", getString(R.string.dialog_wait_content));

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
}
