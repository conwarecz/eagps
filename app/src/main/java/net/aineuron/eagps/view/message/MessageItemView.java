package net.aineuron.eagps.view.message;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.model.database.Message;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;

/**
 * Created by Vit Veres on 19-Jun-17
 * as a part of Android-EAGPS project.
 */

@EViewGroup(R.layout.item_messages_message)
public class MessageItemView extends ConstraintLayout {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");

	@ViewById(R.id.date)
	TextView date;

	@ViewById(R.id.time)
	TextView time;

	@ViewById(R.id.message)
	TextView message;

	public MessageItemView(Context context) {
		super(context);
	}

	public MessageItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MessageItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void bind(Message message) {
		this.date.setText(dateFormat.format(message.getDate()));
		this.time.setText(timeFormat.format(message.getDate()));
		this.message.setText(message.getMessage());
	}
}
