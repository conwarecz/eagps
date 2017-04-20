package net.aineuron.eagps.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.widget.FrameLayout;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.jetradar.multibackstack.BackStackActivity;

import net.aineuron.eagps.R;
import net.aineuron.eagps.fragment.MessagesFragment;
import net.aineuron.eagps.fragment.OrdersFragment;
import net.aineuron.eagps.fragment.StateFragment;
import net.aineuron.eagps.fragment.TowFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity
public class MainActivityBase extends BackStackActivity implements BottomNavigationBar.OnTabSelectedListener {

	private static final String STATE_CURRENT_TAB_ID = "current_tab_id";
	private static final int MAIN_TAB_ID = 0;

	@ViewById(R.id.bottomNavigationBar)
	BottomNavigationBar bottomNavigation;

	@ViewById(R.id.fragmentContainer)
	FrameLayout fragmentContainer;

	private Bundle savedInstanceState = null;
	private Fragment currentFragment;
	private int currentTabId;

	@AfterViews
	public void afterViewsLocal() {
		initBottomNavigation();

		if (savedInstanceState == null) {
			bottomNavigation.selectTab(MAIN_TAB_ID, false);
			showFragment(rootTabFragment(MAIN_TAB_ID));
		}
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.savedInstanceState = savedInstanceState;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
		currentTabId = savedInstanceState.getInt(STATE_CURRENT_TAB_ID);
		bottomNavigation.selectTab(currentTabId, false);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_CURRENT_TAB_ID, currentTabId);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		Pair<Integer, Fragment> pair = popFragmentFromBackStack();
		if (pair != null) {
			backTo(pair.first, pair.second);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onTabSelected(int position) {
		if (currentFragment != null) {
			pushFragmentToBackStack(currentTabId, currentFragment);
		}
		currentTabId = position;
		Fragment fragment = popFragmentFromBackStack(currentTabId);
		if (fragment == null) {
			fragment = rootTabFragment(currentTabId);
		}
		replaceFragment(fragment);
	}

	@Override
	public void onTabUnselected(int position) {

	}

	@Override
	public void onTabReselected(int position) {
		backToRoot();
	}

	public void selectTab(int tabIndex) {
		this.bottomNavigation.selectTab(tabIndex, true);
	}

	public void showFragment(@NonNull Fragment fragment) {
		showFragment(fragment, true);
	}

	public void showFragment(@NonNull Fragment fragment, boolean addToBackStack) {
		if (currentFragment != null && addToBackStack) {
			pushFragmentToBackStack(currentTabId, currentFragment);
		}
		replaceFragment(fragment);
	}

	private void initBottomNavigation() {
		bottomNavigation.setTabSelectedListener(this);

		bottomNavigation
				.setActiveColor(R.color.tabIconHighlight)
				.setInActiveColor(R.color.colorPrimary)
				.setBarBackgroundColor(R.color.backgroundWhite);

		bottomNavigation
				.addItem(new BottomNavigationItem(R.drawable.icon_home, "Odtah"))
				.addItem(new BottomNavigationItem(R.drawable.icon_orders, "Zakázky"))
				.addItem(new BottomNavigationItem(R.drawable.icon_messages, "Zprávy"))
				.setFirstSelectedPosition(0)
				.initialise();
	}

	private void backTo(int tabId, @NonNull Fragment fragment) {
		if (tabId != currentTabId) {
			currentTabId = tabId;
			bottomNavigation.selectTab(currentTabId, false);
		}
		replaceFragment(fragment);
		getSupportFragmentManager().executePendingTransactions();
	}

	private void backToRoot() {
		if (isRootTabFragment(currentFragment, currentTabId)) {
			return;
		}
		resetBackStackToRoot(currentTabId);
		Fragment rootFragment = popFragmentFromBackStack(currentTabId);
		assert rootFragment != null;
		backTo(currentTabId, rootFragment);
	}

	private boolean isRootTabFragment(@NonNull Fragment fragment, int tabId) {
		return fragment.getClass() == rootTabFragment(tabId).getClass();
	}

	private void replaceFragment(@NonNull Fragment fragment) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction tr = fm.beginTransaction();

		tr.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
		tr.replace(R.id.fragmentContainer, fragment);
		tr.commitAllowingStateLoss();
		currentFragment = fragment;
	}

	@NonNull
	private Fragment rootTabFragment(int tabId) {
		switch (tabId) {
			case 0:
				if (MainActivity.STATE.equals(MainActivity.STATE_BUSY_ORDER)) {
					return TowFragment.newInstance();
				} else {
					return StateFragment.newInstance();
				}
			case 1:
				return OrdersFragment.newInstance();
			case 2:
				return MessagesFragment.newInstance();
			default:
				return StateFragment.newInstance();
		}
	}
}
