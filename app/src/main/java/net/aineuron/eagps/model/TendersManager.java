package net.aineuron.eagps.model;

import net.aineuron.eagps.model.database.order.Tender;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Date;
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

	@AfterInject
	void checkOutdatedTenders() {
		Date oldestTenderDate = new Date(System.currentTimeMillis() - (3600 * 1000 * 24)); // -24h

		Realm db = RealmHelper.getTenderDb();
		db.executeTransaction(realm -> {
			RealmResults<Tender> oldTenders = realm.where(Tender.class).lessThan("incomeTime", oldestTenderDate).findAll();
			oldTenders.deleteAllFromRealm();
		});
		db.close();
	}

	public void addTender(final Tender tender) {
		int nextPushId = getNextPushId();
		tender.setPushId(nextPushId);

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

	public Tender getTenderCopy(String tenderEntityUniId) {
		boolean hasTender = false;
		Realm db = RealmHelper.getTenderDb();
		Tender tender = db.where(Tender.class).equalTo("tenderEntityUniId", tenderEntityUniId).findFirst();
		Tender tenderCopy = null;

		if (tender != null) {
			tenderCopy = db.copyFromRealm(tender);
		}
		db.close();
		return tenderCopy;
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

	public boolean hasNextTender() {
		Realm db = RealmHelper.getTenderDb();
		long tenders = db.where(Tender.class).count();
		if (tenders > 0) {
			return true;
		} else {
			return false;
		}
	}

	public List<Integer> getAllPushIds() {
		Realm db = RealmHelper.getTenderDb();
		RealmResults<Tender> all = db.where(Tender.class).findAll();
		List<Integer> pushIds = new ArrayList<>();
		for (Tender tender : all) {
			pushIds.add(tender.getPushId());
		}
		db.close();
		return pushIds;
	}

	public List<Integer> getPushIdsByTenderId(Long tenderId) {
		Realm db = RealmHelper.getTenderDb();
		RealmResults<Tender> all = db.where(Tender.class).equalTo("TenderId", tenderId).findAll();
		List<Integer> pushIds = new ArrayList<>();
		for (Tender tender : all) {
			pushIds.add(tender.getPushId());
		}
		db.close();
		return pushIds;
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

	public boolean hasTenderByTenderId(Long tenderId) {
		boolean hasTender = false;
		Realm db = RealmHelper.getTenderDb();
		if (db.where(Tender.class).equalTo("TenderId", tenderId).findFirst() != null) {
			hasTender = true;
		}
		db.close();
		return hasTender;
	}

	private int getNextPushId() {
		Realm db = RealmHelper.getTenderDb();
		RealmResults<Tender> tenders = db.where(Tender.class).sort("pushId", Sort.ASCENDING).findAll();
		if (tenders.size() == 0) {
			return 1000; // offset
		}

		return tenders.get(tenders.size() - 1).getPushId() + 1;
	}
}
