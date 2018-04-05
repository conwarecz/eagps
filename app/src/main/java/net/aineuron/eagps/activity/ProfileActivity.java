package net.aineuron.eagps.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import net.aineuron.eagps.R;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
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
	IcoLabelTextView telephone;

	private MaterialDialog progressDialog;
	private User user;

	@AfterViews
	public void afterViews() {
		user = userManager.getUser();

		if (user == null) {
			Toast.makeText(this, "No User", Toast.LENGTH_SHORT).show();
			return;
		}

		if (user.getRoleName() != null) {
			roleView.setText(user.getRoleName());
		}
		if (user.getUserName() != null) {
			nameView.setText(user.getUserName());
		}
		if (user.getPhone() != null) {
			telephone.setLabelText("Telefon");
			telephone.setText(user.getPhone());
			telephone.setVisibility(View.VISIBLE);
		} else {
			telephone.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menuProfile.getActionView().setOnClickListener(null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setTitle("Uživatel");

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(getTitle());
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Click(R.id.logoutButton)
	public void logoutClicked() {
		showProgress();
		userManager.logout(user);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onLoggedOutEvent(UserLoggedOutEvent e) {
		userManager.deleteUser();
		clientProvider.rebuildRetrofit();
		dismissDialog();
		showLogin();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onErrorApiEvent(ApiErrorEvent e) {
		dismissDialog();
		super.onErrorApiEvent(e);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onKnownError(KnownErrorEvent e) {
		dismissDialog();
		super.onKnownError(e);
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