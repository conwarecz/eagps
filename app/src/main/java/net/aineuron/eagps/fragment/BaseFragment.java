package net.aineuron.eagps.fragment;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

public class BaseFragment extends Fragment {

	private MaterialDialog progressDialog;

	protected void setAppbarTitle(String title) {
		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (actionBar == null) {
			return;
		}

		actionBar.setTitle(title);
	}

	protected void setAppbarUpNavigation(boolean showBackButton) {
		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (actionBar == null) {
			return;
		}

		actionBar.setDisplayHomeAsUpEnabled(showBackButton);
	}

	protected void showProgress(String title, String content) {
		progressDialog = new MaterialDialog.Builder(getContext())
				.title(title)
				.content(content)
				.cancelable(false)
				.progress(true, 0)
				.show();
	}

	protected void hideProgress() {
		if (progressDialog == null) {
			return;
		}

		if (progressDialog.isCancelled()) {
			return;
		}

		progressDialog.dismiss();
	}
}
