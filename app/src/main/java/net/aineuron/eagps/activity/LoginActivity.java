package net.aineuron.eagps.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity {

	@AfterViews
	public void afterViews() {
		getSupportActionBar().hide();
	}

	@Click(R.id.loginButton)
	public void loginClick() {
		CarSettingsActivity_.intent(this).start();
		finish();
	}

	@Click(R.id.webButton)
	public void webClick() {
		String url = "http://www.europ-assistance.cz";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		try {
			startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Please install a web browser", Toast.LENGTH_SHORT).show();
		}
	}
}
