package net.aineuron.eagps.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import net.aineuron.eagps.R;
import net.aineuron.eagps.fragment.OrderDetailFragment;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import io.realm.Realm;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 21.09.2017.
 */
@EActivity(R.layout.activity_order_confirmation)
public class OrderConfirmationActivity extends AppCompatActivity {

    @Extra
    Long id;
    @Extra
    String title;

    @Bean
    UserManager userManager;

    @Click(R.id.confirmationButton)
    void okClicked() {
        if (userManager.getUser() != null) {
            IntentUtils.openMainActivity(this);
        }
        finish();
    }

    @AfterViews
    void afterViewsLocal() {
        if (id == null) {
            finish();
        }
        Realm db = RealmHelper.getDb();
        Order order = db.where(Order.class).equalTo("id", id).findFirst();
        if (order == null) {
            finish();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.confirmationFragment, OrderDetailFragment.newInstance(id, title));
        fragmentTransaction.commitNow();
    }

}
