package net.aineuron.eagps.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.R;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.event.network.user.FirebaseTokenRefreshedEvent;
import net.aineuron.eagps.event.network.user.UserDataGotEvent;
import net.aineuron.eagps.event.network.user.UserLoggedInEvent;
import net.aineuron.eagps.event.network.user.UserTokenSet;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.transfer.LoginInfo;
import net.aineuron.eagps.util.IntentUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static net.aineuron.eagps.model.UserManager.WORKER_ID;

@EActivity(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener {

	@NotEmpty(messageResId = R.string.login_input_invalid_username)
	@ViewById(R.id.login)
	EditText loginField;

	@NotEmpty(messageResId = R.string.login_input_invalid_password)
	@ViewById(R.id.password)
	EditText passwordField;

	@Bean
	UserManager userManager;

	@EventBusGreenRobot
	EventBus bus;

	@Pref
	Pref_ pref;

	@Bean
	ClientProvider clientProvider;

	private Validator validator;
	private MaterialDialog progressDialog;

	@AfterViews
	public void afterViews() {
		getSupportActionBar().hide();

		validator = new Validator(this);

		if (userManager.getUser() != null) {
			// User logged in
			showProgress();
			setFirebaseToken();
		} else {
			validator.setValidationListener(this);
		}
	}

	@Click(R.id.loginButton)
	public void loginClick() {
		validator.validate();
	}

	@Click(R.id.webButton)
	public void webClick() {
		String url = getString(R.string.webEA);
		IntentUtils.openUrl(this, url);
	}

	@Override
	public void onValidationSucceeded() {
		// Fields ok - attempt login
		showProgress();
		LoginInfo info = new LoginInfo(loginField.getText().toString(), passwordField.getText().toString());
		userManager.login(info);
	}

	@Override
	public void onValidationFailed(List<ValidationError> errors) {
		for (ValidationError error : errors) {
			View view = error.getView();
			String message = error.getCollatedErrorMessage(this);

			// Display error messages ;)
			if (view instanceof EditText) {
				((EditText) view).setError(message);
			} else {
				Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onLoggedInEvent(UserLoggedInEvent e) {
		setFirebaseToken();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginFailed(ApiErrorEvent e) {
        dismissDialog();
        Toast.makeText(this, "Login se nezdařil", Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onKnownError(KnownErrorEvent e) {
        pref.clear();
        dismissDialog();
        Toast.makeText(this, "Login se nezdařil", Toast.LENGTH_SHORT).show();
    }

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onTokenSet(UserTokenSet e) {
		getUserData(e.userId);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onUserDataGot(UserDataGotEvent e) {
		finishLogin();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onFirebaseTokenRefreshed(FirebaseTokenRefreshedEvent e) {
		userManager.setFirebaseToken(e.token);
	}

	private void setFirebaseToken() {
		String token = FirebaseInstanceId.getInstance().getToken();
		if (token != null) {
			userManager.setFirebaseToken(token);
		}
	}

	private void getUserData(Long userId) {
		userManager.getUserData(userId);
	}

	private void finishLogin() {
		User user = userManager.getUser();
		if (progressDialog.isShowing()) {
			dismissDialog();
		}
		if ((user.getUserRole() != null && user.getUserRole() == WORKER_ID) && (user.getEntity() == null || user.getEntity().getEntityId() == null)) {
			CarSettingsActivity_.intent(this).start();
        } else {
            IntentUtils.openMainActivity(this);
        }
		finish();
	}

	private void showProgress() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = new MaterialDialog.Builder(this)
					.title("Přihlašuji stav")
					.content(getString(R.string.dialog_wait_content))
					.cancelable(false)
					.progress(true, 0)
					.show();
		}
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
