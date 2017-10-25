package net.aineuron.eagps.activity;

import android.view.Menu;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main_menu)
public class MainActivity extends AppBarActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        menuProfile.getActionView().setOnClickListener(v -> {
//                    v.setOnClickListener(null);
//                    ProfileActivity_.intent(this).start();
//                }
//        );
        return super.onCreateOptionsMenu(menu);
    }
}
