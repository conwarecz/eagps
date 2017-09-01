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

import static net.aineuron.eagps.adapter.DispatcherSelectCarAdapter.CAR_STATE_BUSY;
import static net.aineuron.eagps.adapter.DispatcherSelectCarAdapter.CAR_STATE_READY;
import static net.aineuron.eagps.adapter.DispatcherSelectCarAdapter.CAR_STATE_UNAVAILABLE;

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
            EventBus.getDefault().post(new WorkerCarSelectedEvent(car.getId(), carRZ.isChecked()));
        });

        switch (car.getStatusId().intValue()) {
            case CAR_STATE_UNAVAILABLE:
                this.getRootView().setBackgroundResource(R.color.unavailable);
                stateIcon.setImageResource(R.drawable.icon_small_unavailable);
                stateIcon.setVisibility(VISIBLE);
                carState.setText(R.string.car_unavailable);
                break;
            case CAR_STATE_BUSY:
                this.getRootView().setBackgroundResource(R.color.busy);
                stateIcon.setImageResource(R.drawable.icon_small_busy);
                stateIcon.setVisibility(VISIBLE);
                carState.setText(R.string.car_on_duty);
                break;
            case CAR_STATE_READY:
                this.getRootView().setBackgroundResource(R.color.ready);
                stateIcon.setVisibility(INVISIBLE);
                carState.setText(R.string.car_waiting);
                break;
            default:
                this.getRootView().setBackgroundResource(R.color.ready);
                stateIcon.setVisibility(INVISIBLE);
                carState.setText(R.string.car_waiting);
                break;
        }
    }

}