package net.aineuron.eagps.activity;

import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.res.ColorRes;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main_menu)
public class MainActivity extends MainActivityBase {

	public static final String STATE_READY = "ready456";
	public static final String STATE_BUSY = "busy5646";
	public static final String STATE_UNAVAILABLE = "unavailable65413";
	public static final String STATE_NO_CAR = "no_car646";
	public static final String STATE_BUSY_ORDER = "busy646";
	public static String STATE = "ready456";

	@ColorRes(R.color.colorPrimary)
	int primary;
	@ColorRes(R.color.ready)
	int ready;
	@ColorRes(R.color.busy)
	int busy;
	@ColorRes(R.color.unavailable)
	int unavailable;

	@OptionsMenuItem(R.id.action_profile)
	MenuItem menuProfile;
	@OptionsMenuItem(R.id.action_state)
	MenuItem menuState;

	private TextView profileName;
	private ImageView stateIcon;
	private TextView licencePlate;

	@AfterInject
	void afterViews() {

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		profileName = (TextView) menuProfile.getActionView().findViewById(R.id.profileName);
		stateIcon = (ImageView) menuState.getActionView().findViewById(R.id.stateIcon);
		licencePlate = (TextView) menuState.getActionView().findViewById(R.id.licenePlate);

		menuProfile.getActionView().setOnClickListener(v -> Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show());
		menuState.getActionView().setOnClickListener(v -> {
			CarSettingsActivity_.intent(this).start();
			finish();
		});

		setUpActionBar();

		return true;
	}

	@OptionsItem(R.id.actin_offer)
	void actionOffer() {
		OfferActivity_.intent(this).start();
		finish();
	}

	private void setUpActionBar() {
		ActionBar actionBar = getSupportActionBar();

		if (actionBar == null) {
			return;
		}

		switch (MainActivity.STATE) {
			case MainActivity.STATE_READY:
				setActionBarColor(actionBar, ready);
				break;
			case MainActivity.STATE_BUSY:
				setActionBarColor(actionBar, busy);
				break;
			case MainActivity.STATE_UNAVAILABLE:
				setActionBarColor(actionBar, unavailable);
				break;
			case MainActivity.STATE_NO_CAR:
				menuState.setVisible(false);
				setActionBarColor(actionBar, primary);
				break;
			default:
				setActionBarColor(actionBar, primary);
				break;
		}
	}

	private void setActionBarColor(@NonNull ActionBar actionBar, int color) {
		ColorDrawable colorDrawable = new ColorDrawable(color);
		actionBar.setBackgroundDrawable(colorDrawable);
	}
}
