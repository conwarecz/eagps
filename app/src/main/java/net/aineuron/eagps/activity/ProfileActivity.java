package net.aineuron.eagps.activity;

import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.view.widget.IcoLabelTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_profile)
public class ProfileActivity extends AppCompatActivity {

	@ViewById(R.id.roleView)
	TextView roleView;

	@ViewById(R.id.nameView)
	TextView nameView;

	@ViewById(R.id.telephoneView)
	IcoLabelTextView profile;

	@AfterViews
	public void afterViews() {
		roleView.setText("Pracovník");
		nameView.setText("Jan Novák");
		profile.setLabelText("Label test");
		profile.setText("Test text");
	}
}