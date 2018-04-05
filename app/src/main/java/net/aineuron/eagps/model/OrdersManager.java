package net.aineuron.eagps.model;

import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.order.LocalPhotos;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.Reasons;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.Nullable;
import io.realm.Realm;
import io.realm.RealmResults;

import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_ASSIGNED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_ENTITY_FINISHED;

/**
 * Created by Vit Veres on 05-Jun-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class OrdersManager {
	@Pref
	Pref_ pref;

	@Bean
	ClientProvider clientProvider;

	public Order getOrderById(Long orderId) {
		Realm db = RealmHelper.getDb();
		Order order = db.where(Order.class).equalTo("id", orderId).findFirst();
		return order;
	}

	public Order getOrderByIdCopy(Long orderId) {
		Realm db = RealmHelper.getDb();
		Order order = db.where(Order.class).equalTo("id", orderId).findFirst();

		Order orderCopy = null;
		if (order != null) {
			orderCopy = db.copyFromRealm(order);
		}
		db.close();
		return orderCopy;
	}

	public void updateOrder(Long orderId) {
		clientProvider.getEaClient().getOrderDetail(orderId);
	}

	public void cancelOrder(Long orderId, Long reason) {
		clientProvider.getEaClient().cancelOrder(orderId, reason);
	}

	public void clearDatabase() {
		Realm db = RealmHelper.getDb();
		db.executeTransaction(realm -> {
			realm.where(Order.class).findAll().deleteAllFromRealm();
			realm.where(Message.class).findAll().deleteAllFromRealm();
			realm.where(LocalPhotos.class).findAll().deleteAllFromRealm();
		});
		db.close();
	}

	public void deleteOrders() {
		Realm db = RealmHelper.getDb();
		db.executeTransaction(realm -> {
			realm.where(Order.class).findAll().deleteAllFromRealm();
		});
		db.close();
	}

	public void sendOrder(Long orderId, Reasons reasons) {
		clientProvider.getEaClient().sendOrder(orderId, reasons);
	}

	public void deleteOrderFromRealm(Long orderId) {
		Realm db = RealmHelper.getDb();
		db.executeTransactionAsync(realm -> {
			Order order = realm.where(Order.class).equalTo("id", orderId).findFirst();
			if (order != null && order.isValid()) {
				order.deleteFromRealm();
			}
		});
		db.close();
	}

	@Nullable
	public Order getFirstActiveOrder() {
		Realm db = RealmHelper.getDb();
		Order order = db.where(Order.class)
				.beginGroup()
				.equalTo("status", ORDER_STATE_ASSIGNED)
				.or()
				.equalTo("status", ORDER_STATE_ENTITY_FINISHED)
				.endGroup()
				.findFirst();
		Order orderCopy = null;
		if (order != null) {
			orderCopy = db.copyFromRealm(order);
		}
		db.close();
		return orderCopy;
	}

	public boolean isActiveOrder(Long id) {
		Realm db = RealmHelper.getDb();
		Order order = db.where(Order.class)
				.equalTo("id", id)
				.beginGroup()
				.equalTo("status", ORDER_STATE_ASSIGNED)
				.or()
				.equalTo("status", ORDER_STATE_ENTITY_FINISHED)
				.endGroup()
				.findFirst();

		boolean isActiveOrder = order != null;
		db.close();

		return isActiveOrder;
	}

	public void addOrder(Order order) {
		Realm db = RealmHelper.getDb();
		db.executeTransaction(realm ->
				realm.copyToRealm(order)
		);
	}

	public List<Integer> getAllPushIds() {
		Realm db = RealmHelper.getDb();
		RealmResults<Order> all = db.where(Order.class).findAll();
		List<Integer> pushIds = new ArrayList<>();
		for (Order order : all) {
			pushIds.add(order.getId().intValue());
		}
		db.close();
		return pushIds;
	}
}
