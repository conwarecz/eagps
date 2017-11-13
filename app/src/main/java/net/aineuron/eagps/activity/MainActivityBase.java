package net.aineuron.eagps.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.ShapeBadgeItem;
import com.jetradar.multibackstack.BackStackActivity;
import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.event.network.MessageStatusChangedEvent;
import net.aineuron.eagps.event.network.car.StateSelectedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.fragment.DispatcherSelectCarFragment;
import net.aineuron.eagps.fragment.DispatcherSelectCarFragment_;
import net.aineuron.eagps.fragment.MessageDetailFragment;
import net.aineuron.eagps.fragment.MessageDetailFragment_;
import net.aineuron.eagps.fragment.MessagesFragment;
import net.aineuron.eagps.fragment.MessagesFragment_;
import net.aineuron.eagps.fragment.NoCarStateFragment;
import net.aineuron.eagps.fragment.NoCarStateFragment_;
import net.aineuron.eagps.fragment.OrderAttachmentsFragment_;
import net.aineuron.eagps.fragment.OrderDetailFragment_;
import net.aineuron.eagps.fragment.OrdersFragment;
import net.aineuron.eagps.fragment.OrdersFragment_;
import net.aineuron.eagps.fragment.StateFragment;
import net.aineuron.eagps.fragment.StateFragment_;
import net.aineuron.eagps.fragment.TowFragment;
import net.aineuron.eagps.fragment.TowFragment_;
import net.aineuron.eagps.model.MessagesManager;
import net.aineuron.eagps.model.UserManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static net.aineuron.eagps.model.UserManager.DISPATCHER_ID;
import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY_ORDER;
import static net.aineuron.eagps.model.UserManager.WORKER_ID;

@EActivity
public class MainActivityBase extends BackStackActivity implements BottomNavigationBar.OnTabSelectedListener {

	public static final int MAIN_TAB_ID = 0;
	public static final int ORDERS_TAB_ID = 1;
	public static final int MESSAGES_TAB_ID = 2;
	private static final String STATE_CURRENT_TAB_ID = "current_tab_id";
	@ViewById(R.id.bottomNavigationBar)
	BottomNavigationBar bottomNavigation;

	@ViewById(R.id.fragmentContainer)
	FrameLayout fragmentContainer;

	@Bean
	UserManager userManager;

	@Bean
	ClientProvider clientProvider;

	@Bean
	MessagesManager messagesManager;

	@Nullable
	@Extra
	Long messageId;

	@Nullable
	@Extra
	boolean cancelTender;

	@EventBusGreenRobot
	EventBus eventBus;

	private Bundle savedInstanceState = null;
	private Fragment currentFragment;
	private int currentTabId;
	private ShapeBadgeItem shapeBadgeItem;

	@AfterViews
	public void afterViewsLocal() {
		if (userManager.getUser() == null) {
			clientProvider.postUnauthorisedError();
		}

        initBottomNavigation();

		if (messageId != null) {
			bottomNavigation.selectTab(MAIN_TAB_ID, false);
			bottomNavigation.selectTab(MESSAGES_TAB_ID, false);
			onTabSelected(MESSAGES_TAB_ID);
			showFragment(MessageDetailFragment.newInstance(messageId));
			messageId = null;
		} else {
			if (savedInstanceState == null) {
				bottomNavigation.selectTab(MAIN_TAB_ID, false);
				showFragment(rootTabFragment(MAIN_TAB_ID));
			}
		}
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.savedInstanceState = savedInstanceState;
	}

	@Override
	protected void onResume() {
		super.onResume();
		userManager.haveActiveOrder();
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
		currentTabId = savedInstanceState.getInt(STATE_CURRENT_TAB_ID);
		bottomNavigation.selectTab(currentTabId, false);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		messageId = intent.getLongExtra("messageId", -1L);
		if (messageId != -1L) {
			onTabSelected(MESSAGES_TAB_ID);
			showFragment(MessageDetailFragment.newInstance(messageId));
		} else {
			for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
				getSupportFragmentManager().popBackStack();
			}
			showFragment(rootTabFragment(MAIN_TAB_ID), false);
			currentTabId = MAIN_TAB_ID;
		}
		bottomNavigation.selectTab(currentTabId, false);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_CURRENT_TAB_ID, currentTabId);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		if (userManager.getUser().getUserRole() == DISPATCHER_ID && currentFragment instanceof OrdersFragment_) {
			// Dispatcher has Orders as default tab - We want to exit app
			super.onBackPressed();
			return;
		} else if ((currentFragment instanceof TowFragment_ && userManager.getUser().getRoleId() == WORKER_ID) || currentFragment instanceof NoCarStateFragment_ || currentFragment instanceof StateFragment_) {
			// Default fragment for worker - We want to exit app
			super.onBackPressed();
			return;
		} else if (currentFragment instanceof OrdersFragment_ || currentFragment instanceof MessagesFragment_ || currentFragment instanceof DispatcherSelectCarFragment_) {
			// In these cases we want to get to the default Tab and its first fragment
			backToDefaultFragment();
			return;
		}

		// All other fragments needs to pop from backstack to previous
		Pair<Integer, Fragment> pair = popFragmentFromBackStack();
		if (pair != null) {
			backTo(pair.first, pair.second);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onTabSelected(int position) {
        if (currentFragment != null && !(currentFragment instanceof MessageDetailFragment_ || currentFragment instanceof OrderDetailFragment_ || currentFragment instanceof OrderAttachmentsFragment_)) {
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
		if (fragment instanceof TowFragment_ && userManager.getUser().getUserRole() == WORKER_ID) {
			selectTab(MAIN_TAB_ID);
			currentTabId = MAIN_TAB_ID;
		} else if (fragment instanceof OrdersFragment_) {
			int tabId = 1;
			if (userManager.getUser().getUserRole() == DISPATCHER_ID) {
				tabId = 0;
			}
			selectTab(tabId);
			currentTabId = tabId;
		}
		showFragment(fragment, true);
        if (userManager.haveActiveOrder() && !userManager.getSelectedStateId().equals(STATE_ID_BUSY_ORDER)) {
            EventBus.getDefault().post(new StateSelectedEvent(STATE_ID_BUSY_ORDER));
        }
    }

	public void showFragment(@NonNull Fragment fragment, boolean addToBackStack) {
		if (currentFragment != null && addToBackStack) {
			pushFragmentToBackStack(currentTabId, currentFragment);
		}
		replaceFragment(fragment);
	}

	private void initBottomNavigation() {
		bottomNavigation.setTabSelectedListener(this);

		BottomNavigationItem messagesItem = new BottomNavigationItem(R.drawable.icon_messages, "Zprávy");

		shapeBadgeItem = new ShapeBadgeItem()
				.setShape(ShapeBadgeItem.SHAPE_OVAL)
				.setSizeInDp(this, 10, 10)
				.setShapeColor(Color.RED)
				.hide();

		messagesItem.setBadgeItem(shapeBadgeItem);

		if (messagesManager.checkUnreadMessage()) {
			shapeBadgeItem.show();
		}

		bottomNavigation
				.setActiveColor(R.color.colorPrimary)
				.setInActiveColor(R.color.grayText)
				.setBarBackgroundColor(R.color.backgroundWhite);
		if (userManager.getUser().getUserRole() != null && userManager.getUser().getUserRole() == WORKER_ID) {

			bottomNavigation
					.addItem(new BottomNavigationItem(R.drawable.icon_home, "Zásah"))
					.addItem(new BottomNavigationItem(R.drawable.icon_orders, "Zakázky"))
					.addItem(messagesItem)
					.setFirstSelectedPosition(0)
					.initialise();
			// Dispatcher mode
		} else {
			bottomNavigation
					.addItem(new BottomNavigationItem(R.drawable.icon_orders, "Zakázky"))
					.addItem(new BottomNavigationItem(R.drawable.icon_big_notselected_50, "Správa vozidel"))
					.addItem(messagesItem)
					.setFirstSelectedPosition(0)
					.initialise();
		}
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
//		assert rootFragment != null;
		if (rootFragment != null) {
			backTo(currentTabId, rootFragment);
		}
	}

	private void backToDefaultFragment() {
		currentTabId = MAIN_TAB_ID;
		bottomNavigation.selectTab(currentTabId, false);

		// Manual cleaning of all stacks - maybe not necessary on others than MAIN_TAB_ID
		resetBackStackToRoot(0);
		resetBackStackToRoot(1);
		resetBackStackToRoot(2);
		resetBackStackToRoot(3);

		Fragment defaultFragment = rootTabFragment(MAIN_TAB_ID);
		showFragment(defaultFragment);
	}

	private boolean isRootTabFragment(@NonNull Fragment fragment, int tabId) {
		return fragment.getClass() == rootTabFragment(tabId).getClass();
	}

	private void replaceFragment(@NonNull Fragment fragment) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction tr = fm.beginTransaction();

		tr.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
		tr.replace(R.id.fragmentContainer, fragment);
		tr.commitNow();
		currentFragment = fragment;
	}

	@NonNull
	private Fragment rootTabFragment(int tabId) {
		if (userManager.getUser().getUserRole() != null && userManager.getUser().getUserRole() == WORKER_ID) {
			// Driver mode
			switch (tabId) {
				case 0:
					if (userManager.haveActiveOrder()) {
						return TowFragment.newInstance(null);
					} else if (userManager.getSelectedStateId().equals(UserManager.STATE_ID_NO_CAR)) {
						return NoCarStateFragment.newInstance();
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
		} else {
			// Dispatcher mode
			switch (tabId) {
				case 0:
					return OrdersFragment.newInstance();
				case 1:
					return DispatcherSelectCarFragment.newInstance();
				case 2:
					return MessagesFragment.newInstance();
				default:
					return OrdersFragment.newInstance();
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void messageStatusChanged(MessageStatusChangedEvent e) {
		if (e.unread) {
			shapeBadgeItem.show();
		} else {
			shapeBadgeItem.hide();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onStateChangedEvent(StateSelectedEvent e) {
		if (currentFragment instanceof StateFragment) {
			replaceFragment(StateFragment.newInstance());
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onOrderCancelled(OrderCanceledEvent e) {
		if (!userManager.haveActiveOrder() && userManager.getUser().getRoleId() == WORKER_ID && currentTabId == MAIN_TAB_ID) {
			bottomNavigation.selectTab(MAIN_TAB_ID, false);
			showFragment(rootTabFragment(MAIN_TAB_ID));
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onErrorApiEvent(ApiErrorEvent e) {
		Toast.makeText(this, e.message, Toast.LENGTH_LONG).show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onKnownError(KnownErrorEvent e) {
		Toast.makeText(this, e.knownError.getMessage(), Toast.LENGTH_LONG).show();
	}
}
