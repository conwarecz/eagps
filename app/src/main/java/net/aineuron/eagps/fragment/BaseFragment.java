package net.aineuron.eagps.fragment;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

public class BaseFragment extends Fragment {
	protected void setAppbarTitle(String title) {
		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (actionBar == null) {
			return;
		}

		actionBar.setTitle(title);
	}
}
