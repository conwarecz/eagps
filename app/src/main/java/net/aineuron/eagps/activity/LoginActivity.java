package net.aineuron.eagps.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import net.aineuron.eagps.R;
import net.aineuron.eagps.model.transfer.LoginInfo;
import net.aineuron.eagps.util.IntentUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener {

	@NotEmpty(messageResId = R.string.login_input_invalid_username)
	@ViewById(R.id.login)
	EditText loginField;

	@NotEmpty(messageResId = R.string.login_input_invalid_password)
	@ViewById(R.id.password)
	EditText passwordField;

	private Validator validator;

	@AfterViews
	public void afterViews() {
		getSupportActionBar().hide();

		validator = new Validator(this);
		validator.setValidationListener(this);
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
		// TODO: Perform call to log in
		LoginInfo info = new LoginInfo(loginField.getText().toString(), passwordField.getText().toString());
		CarSettingsActivity_.intent(this).start();
		finish();
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
}
