package net.aineuron.eagps.view.dispatcherselectcar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.event.ui.WorkerCarSelectedEvent;
import net.aineuron.eagps.model.database.Car;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.greenrobot.eventbus.EventBus;

import static net.aineuron.eagps.model.UserManager.STATE_ID_BUSY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_READY;
import static net.aineuron.eagps.model.UserManager.STATE_ID_UNAVAILABLE;


/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 31.08.2017.
 */

@EViewGroup(R.layout.item_dispatcher_select_car_item)
public class DispatcherSelectCarItemView extends ConstraintLayout {

    @ViewById(R.id.carCheckRZ)
    AppCompatCheckBox carRZ;

    @ViewById(R.id.carCheckStateIcon)
    ImageView stateIcon;

    @ViewById(R.id.carCheckState)
    TextView carState;

    @ColorRes(R.color.busy)
    int busy;

    @ColorRes(R.color.unavailable)
    int unavailable;

    @ColorRes(R.color.ready)
    int ready;

    public DispatcherSelectCarItemView(@NonNull Context context) {
        super(context);
    }

    public void bind(Car car) {
        carRZ.setText(car.getLicencePlate());
        carRZ.setClickable(false);

        View holder = this.getRootView();
        holder.setOnClickListener(view -> {
            carRZ.setChecked(!carRZ.isChecked());
            EventBus.getDefault().post(new WorkerCarSelectedEvent(car.getId(), carRZ.isChecked(), car.getStatusId()));
        });

        int i = car.getStatusId().intValue();
        if (i == STATE_ID_UNAVAILABLE) {
            this.getRootView().setBackgroundResource(R.color.unavailable);
            stateIcon.setImageResource(R.drawable.icon_small_unavailable);
            stateIcon.setVisibility(VISIBLE);
            carState.setText(R.string.car_unavailable);

        } else if (i == STATE_ID_BUSY) {
            this.getRootView().setBackgroundResource(R.color.busy);
            stateIcon.setImageResource(R.drawable.icon_small_busy);
            stateIcon.setVisibility(VISIBLE);
            carState.setText(R.string.car_on_duty);

        } else if (i == STATE_ID_READY) {
            this.getRootView().setBackgroundResource(R.color.ready);
            stateIcon.setVisibility(INVISIBLE);
            carState.setText(R.string.car_waiting);

        } else {
            this.getRootView().setBackgroundResource(R.color.ready);
            stateIcon.setVisibility(INVISIBLE);
            carState.setText(R.string.car_waiting);

        }
    }

}