package net.aineuron.eagps.fragment;

import android.support.v4.app.Fragment;

import net.aineuron.eagps.R;

import org.androidannotations.annotations.EFragment;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_state)
public class StateFragment extends Fragment {
	public static StateFragment newInstance() {
		return StateFragment_.builder().build();
	}
}
