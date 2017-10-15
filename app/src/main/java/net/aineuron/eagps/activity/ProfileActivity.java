package net.aineuron.eagps.activity;

import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import net.aineuron.eagps.R;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.user.UserLoggedOutEvent;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.view.widget.IcoLabelTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@EActivity(R.layout.activity_profile)
@OptionsMenu(R.menu.main_menu)
public class ProfileActivity extends AppBarActivity {

	@ViewById(R.id.roleView)
	TextView roleView;

	@ViewById(R.id.nameView)
	TextView nameView;

	@ViewById(R.id.telephoneView)
	IcoLabelTextView profile;

	private MaterialDialog progressDialog;
	private User user;

	@AfterViews
	public void afterViews() {
		user = userManager.getUser();
		if (user == null) {
			Toast.makeText(this, "No User", Toast.LENGTH_SHORT).show();
			return;
		}

		roleView.setText(user.getRoleName());
		nameView.setText(user.getUserName());
		profile.setLabelText("Telefon");
		profile.setText(user.getPhone());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menuProfile.getActionView().setOnClickListener(null);

		return true;
	}

	@Click(R.id.logoutButton)
	public void logoutClicked() {
		showProgress();
		userManager.logout(user);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onLoggedOutEvent(UserLoggedOutEvent e) {
		dismissDialog();
		showLogin();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onLoginFailed(ApiErrorEvent e) {
		dismissDialog();
		Toast.makeText(this, e.throwable.getMessage(), Toast.LENGTH_SHORT).show();
	}

	private void showLogin() {
		LoginActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK).start();
		finish();
	}

	private void showProgress() {
		progressDialog = new MaterialDialog.Builder(this)
				.title("Odhlašuji stav")
				.content(getString(R.string.dialog_wait_content))
                .cancelable(false)
                .progress(true, 0)
                .show();
	}

	private void dismissDialog() {
		if (progressDialog == null) {
			return;
		}

		if (!progressDialog.isShowing()) {
			return;
		}
		progressDialog.dismiss();
	}
}