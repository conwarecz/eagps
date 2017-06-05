package net.aineuron.eagps.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.offer.Offer;
import net.aineuron.eagps.model.viewmodel.OfferManager;

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

	@ViewById(R.id.accept)
	Button accept;

	@ViewById(R.id.decline)
	Button decline;

	@ViewById(R.id.showOnMap)
	Button showOnMap;

	@Bean
	UserManager userManager;

	@Bean
	OfferManager offerManager;

	@EventBusGreenRobot
	EventBus bus;

	private MaterialDialog progressDialog;
	private Offer offer;

	@AfterViews
	void afterViews() {
		getSupportActionBar().hide();
		offer = offerManager.getOfferById(16385l);
	}

	@Click(R.id.accept)
	void acceptClicked() {
		showProgress();
		userManager.setStateBusyOnOrder();
	}

	@Click(R.id.decline)
	void declineClicked() {
		// State is the same as before
		MainActivity_.intent(this).start();
		finish();
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


	@Click({R.id.showOnMap, R.id.adress})
	void openMap() {
		double latitude = 49.7751573;
		double longitude = 18.4377711;
		String label = "Jan Novák, tel.: 777 888 999";
		String uriBegin = "geo:" + latitude + "," + longitude;
		String query = latitude + "," + longitude + "(" + label + ")";
		String encodedQuery = Uri.encode(query);
		String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
		Uri uri = Uri.parse(uriString);
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
		try {
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Please install Google Maps application", Toast.LENGTH_SHORT).show();
		}
	}

	private void showProgress() {
		progressDialog = new MaterialDialog.Builder(this)
				.title("Měním stav")
				.content("Prosím čekejte...")
				.cancelable(false)
				.progress(true, 0)
				.show();
	}

	private void finishOfferActivity() {
		progressDialog.dismiss();
		MainActivity_.intent(this).start();
		finish();
	}
}
