package net.aineuron.eagps.view.message;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.Appl;
import net.aineuron.eagps.R;
import net.aineuron.eagps.event.ui.MessageClickedEvent;
import net.aineuron.eagps.model.database.Message;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by Vit Veres on 19-Jun-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.item_messages_message)
public class MessageItemView extends ConstraintLayout {

	@ViewById(R.id.date)
	TextView date;

	@ViewById(R.id.time)
	TextView time;

	@ViewById(R.id.message)
	TextView message;

	@EventBusGreenRobot
	EventBus bus;

	public MessageItemView(Context context) {
		super(context);
	}

	public MessageItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MessageItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void bind(final Message message) {
		this.date.setText(Appl.dateFormat.format(message.getDate()));
		this.time.setText(Appl.timeFormat.format(message.getDate()));
		this.message.setText(message.getMessage());

		this.setOnClickListener(v -> bus.post(new MessageClickedEvent(message.getId())));
	}
}
