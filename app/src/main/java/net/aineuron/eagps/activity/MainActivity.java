package net.aineuron.eagps.activity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main_menu)
public class MainActivity extends MainActivityBase {

	@OptionsMenuItem(R.id.action_profile)
	MenuItem menuProfile;

	private TextView profileName;

	@AfterInject
	void afterViews() {

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		profileName = (TextView) menuProfile.getActionView().findViewById(R.id.profileName);

		menuProfile.getActionView().setOnClickListener(item -> Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show());

		return true;
	}
}
