package net.aineuron.eagps.model;

import net.aineuron.eagps.model.database.order.Tender;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 06.11.2017.
 */

@EBean(scope = EBean.Scope.Singleton)
public class TendersManager {
	public static final String TAG = TendersManager.class.getName();

	public void addTender(final Tender tender) {
		Realm db = RealmHelper.getTenderDb();
		db.executeTransaction(realm -> realm.copyToRealmOrUpdate(tender));
		db.close();
	}

	public void deleteTendersByTenderId(Long tenderId) {
		Realm db = RealmHelper.getTenderDb();
		db.executeTransaction(realm -> {
			RealmResults<Tender> tenders = realm.where(Tender.class).equalTo("TenderId", tenderId).findAll();
			tenders.deleteAllFromRealm();
		});
		db.close();
	}

	public void deleteTendersByEntityId(Long entityId) {
		Realm db = RealmHelper.getTenderDb();
		db.executeTransaction(realm -> {
			RealmResults<Tender> tenders = realm.where(Tender.class).equalTo("Entity.id", entityId).findAll();
			tenders.deleteAllFromRealm();
		});
		db.close();
	}

	public void deleteAllTenders() {
		Realm db = RealmHelper.getTenderDb();
		db.executeTransactionAsync(realm -> {
			RealmResults<Tender> tenders = realm.where(Tender.class).findAll();
			tenders.deleteAllFromRealm();
		});
		db.close();
	}

	public void deleteTender(String tenderEntityUniId) {
		Realm db = RealmHelper.getTenderDb();
		Tender tender = db.where(Tender.class).equalTo("tenderEntityUniId", tenderEntityUniId).findFirst();
		if (tender != null) {
			db.executeTransaction(realm -> tender.deleteFromRealm());
		}
		db.close();
	}

	public Tender getNextTenderCopy() {
		Realm db = RealmHelper.getTenderDb();
		RealmResults<Tender> tenders = db.where(Tender.class).sort("incomeTime", Sort.ASCENDING).findAll();
		if (tenders.size() > 0) {
			return db.copyFromRealm(tenders.get(0));
		} else {
			return null;
		}
	}

	public List<Long> getTenderIds() {
		Realm db = RealmHelper.getTenderDb();
		RealmResults<Tender> all = db.where(Tender.class).findAll();
		List<Long> tenderIds = new ArrayList<>();
		for (Tender tender : all) {
			tenderIds.add(tender.getTenderId());
		}
		db.close();
		return tenderIds;
	}

	public boolean hasTender(String tenderEntityUniId) {
		boolean hasTender = false;
		Realm db = RealmHelper.getTenderDb();
		if (db.where(Tender.class).equalTo("tenderEntityUniId", tenderEntityUniId).findFirst() != null) {
			hasTender = true;
		}
		db.close();
		return hasTender;
	}
}
