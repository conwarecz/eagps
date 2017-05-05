package net.aineuron.eagps.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_offer)
public class OfferActivity extends AppCompatActivity {

	@ViewById(R.id.accept)
	Button accept;

	@ViewById(R.id.decline)
	Button decline;

	@ViewById(R.id.showOnMap)
	Button showOnMap;

	@AfterViews
	void afterViews() {
		getSupportActionBar().hide();
	}

	@Click(R.id.accept)
	void acceptClicked() {
		MainActivity.STATE = MainActivity.STATE_BUSY_ORDER;
		MainActivity_.intent(this).start();
		finish();
	}

	@Click(R.id.decline)
	void declineClicked() {
		MainActivity.STATE = MainActivity.STATE_READY;
		MainActivity_.intent(this).start();
		finish();
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
}
