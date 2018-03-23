package net.aineuron.eagps.activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.aineuron.eagps.R;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.fragment.NoCarStateFragment_;
import net.aineuron.eagps.fragment.StateFragment_;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.User;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.ColorRes;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
	private ActionBar actionBar;

	private boolean didOnCreateOptionsMenuRun;

	@Override
	protected void onResume() {
		super.onResume();
		setUpActionBar();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		this.didOnCreateOptionsMenuRun = true;

		profileName = menuProfile.getActionView().findViewById(R.id.nameView);
		stateIcon = menuState.getActionView().findViewById(R.id.stateIcon);
		licencePlate = menuState.getActionView().findViewById(R.id.licensePlate);

		menuProfile.getActionView().setOnClickListener(v ->
				ProfileActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP).start()
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void stateChangedEvent(StateSelectedEvent e) {
		setUpActionBar();
	}

	public void setUpActionBar() {
		if (!didOnCreateOptionsMenuRun) {
			return;
		}

		actionBar = getSupportActionBar();

		if (actionBar == null) {
			return;
		}

		setUpActionBarTitle();

		menuState.setVisible(true);

		Long i = userManager.getSelectedStateId();

		if (i == null) {
			setActionBarColor(primary);
		} else if (i.equals(UserManager.STATE_ID_READY)) {
			setActionBarColor(ready);
			stateIcon.setImageResource(R.drawable.icon_ready);
		} else if (i.equals(UserManager.STATE_ID_BUSY)) {
			setActionBarColor(busy);
			stateIcon.setImageResource(R.drawable.icon_busy);
		} else if (i.equals(UserManager.STATE_ID_UNAVAILABLE)) {
			setActionBarColor(unavailable);
			stateIcon.setImageResource(R.drawable.icon_unavailable);
		} else if (i.equals(UserManager.STATE_ID_NO_CAR)) {
			menuState.setVisible(false);
			setActionBarColor(primary);
		} else if (i.equals(UserManager.STATE_ID_BUSY_ORDER)) {
			setActionBarColor(busy);
			stateIcon.setImageResource(R.drawable.icon_busy);
		} else {
			setActionBarColor(primary);
		}

		if (userManager.getUser().getUserRole() == null || userManager.getUser().getUserRole() == DISPATCHER_ID) {
			stateIcon.setVisibility(View.GONE);

			licencePlate.setVisibility(View.GONE);
			setActionBarColor(primary);
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

	private void setUpActionBarTitle() {
		if (userManager.getUser() == null || userManager.getUser().getRoleId() == DISPATCHER_ID) {
			return;
		}

		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
		// Update name only on some screens. We want title change only at state and nocar fragment
		if (!(this instanceof MainActivity_) &&
				(
						!(fragment instanceof NoCarStateFragment_) &&
								!(fragment instanceof StateFragment_)
				)
				) {
			return;
		}

		Long i = userManager.getSelectedStateId();

		if (i == null) {
			actionBar.setTitle(getTitle());
		} else if (i.equals(UserManager.STATE_ID_READY)) {
			actionBar.setTitle(R.string.car_waiting);
		} else if (i.equals(UserManager.STATE_ID_BUSY)) {
			actionBar.setTitle(R.string.car_busy);
		} else if (i.equals(UserManager.STATE_ID_UNAVAILABLE)) {
			actionBar.setTitle(R.string.car_unavailable);
		} else if (i.equals(UserManager.STATE_ID_NO_CAR)) {
			actionBar.setTitle(getTitle());
		} else if (i.equals(UserManager.STATE_ID_BUSY_ORDER)) {
			actionBar.setTitle(R.string.car_on_order);
		} else {
			actionBar.setTitle(getTitle());
		}
	}

	private void setActionBarColor(int color) {
		ColorDrawable colorDrawable = new ColorDrawable(color);
		actionBar.setBackgroundDrawable(colorDrawable);
	}
}
