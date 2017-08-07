package net.aineuron.eagps.activity;

import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.model.OfferManager;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.ClientCar;
import net.aineuron.eagps.model.database.order.DestinationAddress;
import net.aineuron.eagps.model.database.order.Offer;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.view.widget.IcoLabelTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@EActivity(R.layout.activity_offer)
public class OfferActivity extends AppCompatActivity {

	@ViewById(R.id.back)
	Button accept;

	@ViewById(R.id.decline)
	Button decline;

	@Bean
	UserManager userManager;

	@Bean
	OfferManager offerManager;

	@Bean
	OrdersManager ordersManager;

	@EventBusGreenRobot
	EventBus bus;

	@ViewById(R.id.clientCar)
	IcoLabelTextView clientCar;
	@ViewById(R.id.clientAddress)
	IcoLabelTextView clientAddress;
	@ViewById(R.id.destinationAddress)
	IcoLabelTextView destinationAddress;
	@ViewById(R.id.eventDescription)
	IcoLabelTextView eventDescription;

	private MaterialDialog progressDialog;
	private Offer offer;

	@AfterViews
	void afterViews() {
		getSupportActionBar().hide();
		offer = offerManager.getOfferById(16385l);

		setUi();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkStateSelectedEvent(StateSelectedEvent e) {
		finishOfferActivity();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkStateSelectedEvent(ApiErrorEvent e) {
		Toast.makeText(this, e.throwable.getMessage(), Toast.LENGTH_SHORT).show();
		finishOfferActivity();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onOfferCanceledEvent(OrderCanceledEvent e) {
		finishOfferActivity();
	}

	@Click(R.id.back)
	void acceptClicked() {
		showProgress("Měním stav", "Prosím čekejte...");
		userManager.setStateBusyOnOrder();
	}

	@Click(R.id.decline)
	void declineClicked() {
		// State is the same as before
		new MaterialDialog.Builder(this)
				.title("Důvod zrušení")
				.items(R.array.order_cancel_choices)
				.itemsIds(R.array.order_cancel_choice_ids)
				.itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
					showProgress("Ruším zakázku", "Prosím čekejte...");
					ordersManager.cancelOrder(offer.getId());
					return true;
				})
				.positiveText("OK")
				.show();
	}

	@Click(R.id.showOnMap)
	void openMap() {
		IntentUtils.openRoute(this, offer.getDestinationAddress().getAddress().getLocation(), offer.getClientAddress().getLocation());
	}

	@Click(R.id.clientAddress)
	void openMapClient() {
		IntentUtils.openMapLocation(this, offer.getClientAddress().getLocation(), offer.getClientName());
	}

	@Click(R.id.destinationAddress)
	void openMapDestination() {
		IntentUtils.openMapLocation(this, offer.getDestinationAddress().getAddress().getLocation(), offer.getDestinationAddress().getName());
	}

	private void setUi() {
		ClientCar car = offer.getCar();
		this.clientCar.setText(car.getModel() + ", " + car.getWeight() + " t");

		Address clientAddress = offer.getClientAddress();
		this.clientAddress.setText(clientAddress.getStreet() + ", " + clientAddress.getCity() + ", " + clientAddress.getZipCode());

		DestinationAddress destinationAddress = offer.getDestinationAddress();
		this.destinationAddress.setText(destinationAddress.getName() + ", " + destinationAddress.getAddress().getStreet() + ", " + destinationAddress.getAddress().getCity() + ", " + destinationAddress.getAddress().getZipCode());

		this.eventDescription.setText(offer.getEventDescription());
	}

	private void finishOfferActivity() {
		hideProgress();
		IntentUtils.openMainActivity(this);
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
}
