package net.aineuron.eagps.view.workerselectcar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatRadioButton;

import net.aineuron.eagps.R;
import net.aineuron.eagps.model.Car;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 */
@EViewGroup(R.layout.item_worker_select_car_item)
public class WorkerSelectCarItemView extends BaseWorkerSelectCarItemView {

	@ViewById(R.id.carRadioText)
	AppCompatRadioButton carRadioText;

	@ColorRes(R.color.colorPrimary)
	int blue;

	@ColorRes(R.color.grayText)
	int gray;

	public WorkerSelectCarItemView(@NonNull Context context) {
		super(context);
	}

	@Override
	protected void bindView() {
		Car car = item.getCar();
		boolean isSelected = item.isSelected();

		carRadioText.setChecked(isSelected);
		carRadioText.setText(String.format("%s - %s", car.getLicensePlate(), car.getModel()));

		if (isSelected) {
			carRadioText.setTextColor(blue);
		} else {
			carRadioText.setTextColor(gray);
		}
	}
}
