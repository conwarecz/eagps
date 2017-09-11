package net.aineuron.eagps.fragment;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.adapter.OrdersAdapter;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

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

	@Bean
	ClientProvider clientProvider;

	private Realm db;
	private RealmResults<Order> ordersRealmQuery;
	private OrdersAdapter adapter;

	public static OrdersFragment newInstance() {
		return OrdersFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(false);
		setAppbarTitle("Zakázky");

		db = RealmHelper.getDb();
//		RealmResults<Order> buffer;
        ordersRealmQuery = db.where(Order.class).findAllSorted("timeCreated", Sort.DESCENDING);

//		db.executeTransaction(realm -> {
//			// TODO: Pouze pro testování stavů, dále odstranit
//			int i = 0;
//			for (Order order : ordersRealmQuery) {
////				order.setSent(i%2 == 0);
////				if(i % 3 == 0){
////					order.setPhotosProvided(true);
////					order.setOrderSheetProvided(true);
////				}
//				order.setStatus((i % 4) + 2);
//				i++;
//			}
//		});

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

        clientProvider.getEaClient().updateOrders();
    }
}
