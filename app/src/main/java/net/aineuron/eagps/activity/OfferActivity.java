package net.aineuron.eagps.activity;

import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_offer)
public class OfferActivity extends AppCompatActivity {

	@ViewById(R.id.offerImage)
	ImageView offerImage;

	@AfterViews
	void afterViews() {
		getSupportActionBar().hide();
	}

	@Click(R.id.offerImage)
	void offerImageClicked() {
		MainActivity.STATE = MainActivity.STATE_BUSY_ORDER;
		MainActivity_.intent(this).start();
		finish();
	}
}
