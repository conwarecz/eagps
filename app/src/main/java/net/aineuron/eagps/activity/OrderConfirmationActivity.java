package net.aineuron.eagps.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import net.aineuron.eagps.R;
import net.aineuron.eagps.fragment.OrderDetailFragment;
import net.aineuron.eagps.util.IntentUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 21.09.2017.
 */
@EActivity(R.layout.activity_order_confirmation)
public class OrderConfirmationActivity extends AppCompatActivity {

    @Extra
    Long id;
    @Extra
    String title;

    @Click(R.id.confirmationButton)
    void okClicked() {
        IntentUtils.openMainActivity(this);
        finish();
    }

    @AfterViews
    void afterViewsLocal() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.confirmationFragment, OrderDetailFragment.newInstance(id, title));
        fragmentTransaction.commitNow();
    }

}
