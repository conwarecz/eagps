package net.aineuron.eagps.activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.aineuron.eagps.R;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.User;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.ColorRes;

import static net.aineuron.eagps.model.UserManager.DISPATCHER_ID;

/**
 * Created by Vit Veres on 30-May-17
 * as a part of Android-EAGPS project.
 */

@EActivity
public class AppBarActivity extends MainActivityBase {

	@SystemService
	NotificationManager notificationManager;

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

	private int currentNotificationID = 0;

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		profileName = menuProfile.getActionView().findViewById(R.id.nameView);
		stateIcon = menuState.getActionView().findViewById(R.id.stateIcon);
		licencePlate = menuState.getActionView().findViewById(R.id.licensePlate);

		menuProfile.getActionView().setOnClickListener(v ->
				ProfileActivity_.intent(this).start()
		);
		menuState.getActionView().setOnClickListener(v -> {
			if (userManager.getSelectedStateId().equals(UserManager.STATE_ID_BUSY_ORDER) || userManager.haveActiveOrder()) {
				Toast.makeText(this, "Při aktivní zakázce nelze měnit vůz!", Toast.LENGTH_LONG).show();
				return;
			}
			CarSettingsActivity_.intent(this).resetCar(true).start();
		});

		setUpActionBar();

		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setUpActionBar();
	}

	private void setUpActionBar() {
		ActionBar actionBar = getSupportActionBar();

		if (actionBar == null) {
			return;
		}

		menuState.setVisible(true);

		Long i = userManager.getSelectedStateId();
		if (i == null) {
			setActionBarColor(actionBar, primary);

		} else if (i.equals(UserManager.STATE_ID_READY)) {
			setActionBarColor(actionBar, ready);
			stateIcon.setImageResource(R.drawable.icon_ready);

		} else if (i.equals(UserManager.STATE_ID_BUSY)) {
			setActionBarColor(actionBar, busy);
			stateIcon.setImageResource(R.drawable.icon_busy);

		} else if (i.equals(UserManager.STATE_ID_UNAVAILABLE)) {
			setActionBarColor(actionBar, unavailable);
			stateIcon.setImageResource(R.drawable.icon_unavailable);

		} else if (i.equals(UserManager.STATE_ID_NO_CAR)) {
			menuState.setVisible(false);
			setActionBarColor(actionBar, primary);

		} else if (i.equals(UserManager.STATE_ID_BUSY_ORDER)) {
			setActionBarColor(actionBar, busy);
			stateIcon.setImageResource(R.drawable.icon_busy);

		} else {
			setActionBarColor(actionBar, primary);
		}

        if (userManager.getUser().getUserRole() == null || userManager.getUser().getUserRole() == DISPATCHER_ID) {
            stateIcon.setVisibility(View.GONE);
            licencePlate.setVisibility(View.GONE);
            setActionBarColor(actionBar, primary);
        } else {
            stateIcon.setVisibility(View.VISIBLE);
            licencePlate.setVisibility(View.VISIBLE);
        }

		User user = userManager.getUser();
		if (user != null) {
			profileName.setText(user.getUserName());

			if (user.getEntity() != null) {
				String licensePlate = user.getEntity().getLicencePlate();
				licencePlate.setText(licensePlate);
			}
		}
	}

	private void setActionBarColor(@NonNull ActionBar actionBar, int color) {
		ColorDrawable colorDrawable = new ColorDrawable(color);
		actionBar.setBackgroundDrawable(colorDrawable);
	}
}
