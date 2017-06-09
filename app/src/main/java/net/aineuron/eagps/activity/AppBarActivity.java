package net.aineuron.eagps.activity;

import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.User;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.res.ColorRes;

/**
 * Created by Vit Veres on 30-May-17
 * as a part of Android-EAGPS project.
 */

@EActivity
public class AppBarActivity extends MainActivityBase {

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

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		profileName = (TextView) menuProfile.getActionView().findViewById(R.id.nameView);
		stateIcon = (ImageView) menuState.getActionView().findViewById(R.id.stateIcon);
		licencePlate = (TextView) menuState.getActionView().findViewById(R.id.licensePlate);

		menuProfile.getActionView().setOnClickListener(v -> ProfileActivity_.intent(this).start());
		menuState.getActionView().setOnClickListener(v -> {
			CarSettingsActivity_.intent(this).resetCar(true).start();
			finish();
		});

		setUpActionBar();

		return true;
	}

	@OptionsItem(R.id.actin_offer)
	void actionOffer() {
		OfferActivity_.intent(this).start();
	}

	private void setUpActionBar() {
		ActionBar actionBar = getSupportActionBar();

		if (actionBar == null) {
			return;
		}

		switch (userManager.getSelectedStateId()) {
			case UserManager.STATE_ID_READY:
				setActionBarColor(actionBar, ready);
				stateIcon.setImageResource(R.drawable.icon_ready);
				break;
			case UserManager.STATE_ID_BUSY:
				setActionBarColor(actionBar, busy);
				stateIcon.setImageResource(R.drawable.icon_busy);
				break;
			case UserManager.STATE_ID_UNAVAILABLE:
				setActionBarColor(actionBar, unavailable);
				stateIcon.setImageResource(R.drawable.icon_unavailable);
				break;
			case UserManager.STATE_ID_NO_CAR:
				menuState.setVisible(false);
				setActionBarColor(actionBar, primary);
				break;
			case UserManager.STATE_ID_BUSY_ORDER:
				setActionBarColor(actionBar, busy);
				stateIcon.setImageResource(R.drawable.icon_busy);
				break;
			default:
				setActionBarColor(actionBar, primary);
				break;
		}

		User user = userManager.getUser();
		profileName.setText(user.getName());

		if (user.getCar() != null) {
			String licensePlate = user.getCar().getLicensePlate();
			licencePlate.setText(licensePlate);
		}
	}

	private void setActionBarColor(@NonNull ActionBar actionBar, int color) {
		ColorDrawable colorDrawable = new ColorDrawable(color);
		actionBar.setBackgroundDrawable(colorDrawable);
	}
}
