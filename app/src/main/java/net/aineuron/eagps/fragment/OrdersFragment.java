package net.aineuron.eagps.fragment;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.adapter.OrdersAdapter;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.order.OrderAcceptedEvent;
import net.aineuron.eagps.event.network.order.OrderCanceledEvent;
import net.aineuron.eagps.event.ui.StopRefreshingEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.transfer.Paging;
import net.aineuron.eagps.util.RealmHelper;
import net.aineuron.eagps.view.EndlessRecyclerViewScrollListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_orders)
public class OrdersFragment extends BaseFragment {

	@ViewById(R.id.ordersView)
	RecyclerView ordersView;

	@ViewById(R.id.ordersSwipe)
	SwipeRefreshLayout swipeRefreshLayout;

	@Bean
	ClientProvider clientProvider;

	@Bean
	OrdersManager ordersManager;

	private Realm db;
	private RealmResults<Order> ordersRealmQuery;
	private OrdersAdapter adapter;
	private Paging paging;

	public static OrdersFragment newInstance() {
		return OrdersFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(false);
		setAppbarTitle("Zakázky");

		paging = new Paging();

		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});

		db = RealmHelper.getDb();
        ordersRealmQuery = db.where(Order.class).findAllSorted("timeCreated", Sort.DESCENDING);

//		ordersRealmQuery = db.where(Order.class).equalTo("status", ORDER_STATE_ASSIGNED).findAllSorted("timeCreated", Sort.DESCENDING);
//		buffer = db.where(Order.class).equalTo("status", ORDER_STATE_FINISHED).findAllSorted("timeCreated", Sort.DESCENDING);
//		ordersRealmQuery.addAll(buffer);
//		buffer = db.where(Order.class).equalTo("status", ORDER_STATE_SENT).findAllSorted("timeCreated", Sort.DESCENDING);
//		ordersRealmQuery.addAll(buffer);
//		buffer = db.where(Order.class).equalTo("status", ORDER_STATE_CANCELLED).findAllSorted("timeCreated", Sort.DESCENDING);
//		ordersRealmQuery.addAll(buffer);

//		ordersRealmQuery = db.where(Order.class).findAllSorted("timeCreated", Sort.DESCENDING); //.equalTo("status", ORDER_STATE_ASSIGNED).findAll().where(Order.class).equalTo("status", ORDER_STATE_FINISHED).equalTo("status", ORDER_STATE_SENT).equalTo("status", ORDER_STATE_CANCELLED).findAllSorted("timeCreated", Sort.DESCENDING);

		adapter = new OrdersAdapter(ordersRealmQuery);
		adapter.setMainActivityBase((MainActivityBase) getActivity());

		LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
		DividerItemDecoration decor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.horizontal_divider_gray);
		decor.setDrawable(horizontalDivider);

		ordersView.setLayoutManager(manager);
		ordersView.addItemDecoration(decor);

		ordersView.setAdapter(adapter);

		ordersView.addOnScrollListener(new EndlessRecyclerViewScrollListener(manager) {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				paging.nextPage();
				swipeRefreshLayout.setRefreshing(true);
				clientProvider.getEaClient().updateOrders(paging);
			}
		});

		swipeRefreshLayout.setRefreshing(true);
		clientProvider.getEaClient().updateOrders(paging);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onStopRefreshing(StopRefreshingEvent e) {
		if (paging.getSkip() == 0 && adapter.getItemCount() <= 10) {
			ordersView.getLayoutManager().scrollToPosition(0);
		}
		swipeRefreshLayout.setRefreshing(false);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCancelledOrder(OrderCanceledEvent e) {
		refresh();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onOrderAccepted(OrderAcceptedEvent e) {
		refresh();
	}


	private void refresh() {
		paging = new Paging();
		ordersManager.deleteOrders();
		clientProvider.getEaClient().updateOrders(paging);
	}
}
