package net.aineuron.eagps.model;

import net.aineuron.eagps.model.database.order.Tender;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.EBean;

import java.util.Date;

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

    public void deleteAllOtherTenders(Long tenderId) {
        Realm db = RealmHelper.getTenderDb();
        db.executeTransactionAsync(realm -> {
            RealmResults<Tender> tenders = realm.where(Tender.class).equalTo("TenderId", tenderId).findAll();
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

    public void deleteTender(Long tenderId, Date incomeDate) {
        Realm db = RealmHelper.getTenderDb();
        Tender tender = db.where(Tender.class).equalTo("TenderId", tenderId).equalTo("incomeTime", incomeDate).findFirst();
        db.executeTransaction(realm -> tender.deleteFromRealm());
        db.close();
    }

    public Tender getNextTender(Long tenderId) {
        Realm db = RealmHelper.getTenderDb();
        RealmResults<Tender> tenders = db.where(Tender.class).equalTo("TenderId", tenderId).findAllSorted("incomeTime", Sort.ASCENDING);
        if (tenders.size() > 0) {
            return tenders.get(0);
        } else {
            return null;
        }
    }

    public boolean isNextTender(Long tenderId) {
        boolean next = false;
        Realm db = RealmHelper.getTenderDb();
        if (db.where(Tender.class).equalTo("TenderId", tenderId).findFirst() != null) {
            next = true;
        }
        db.close();
        return next;
    }
}
