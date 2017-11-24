package net.aineuron.eagps.fragment;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.event.ui.StopRefreshingEvent;

import org.androidannotations.annotations.EFragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EFragment
public class BaseFragment extends Fragment {

	@EventBusGreenRobot
	EventBus eventBus;

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
        try {
            progressDialog = new MaterialDialog.Builder(getContext())
                    .title(title)
                    .content(content)
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void dismissProgress() {
        if (progressDialog == null) {
			return;
		}

		if (progressDialog.isCancelled()) {
			return;
		}

		progressDialog.dismiss();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void stopRefreshing(StopRefreshingEvent e) {
        dismissProgress();
    }
}
