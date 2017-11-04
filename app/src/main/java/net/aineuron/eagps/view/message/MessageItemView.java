package net.aineuron.eagps.view.message;

import android.content.Context;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.Appl;
import net.aineuron.eagps.R;
import net.aineuron.eagps.event.ui.MessageClickedEvent;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.util.IntentUtils;

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
		if (!message.isRead()) {
			this.message.setTypeface(Typeface.create(this.message.getTypeface(), Typeface.NORMAL), Typeface.BOLD);
		} else {
			this.message.setTypeface(Typeface.create(this.message.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
		}

        if (message.getTime() != null) {

			this.date.setText(Appl.dateFormat.format(message.getTime()));
			this.time.setText(Appl.timeFormat.format(message.getTime()));
        }
        this.message.setText(message.getText());

		this.setOnClickListener(v -> bus.post(new MessageClickedEvent(message.getId())));

		this.setOnLongClickListener(view -> IntentUtils.shareText(super.getContext(), message.getText()));
	}
}
