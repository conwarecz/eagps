package net.aineuron.eagps.fragment;

import android.widget.TextView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;

/**
 * Created by Vit Veres on 20-Jun-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_message_detail)
public class MessageDetailFragment extends BaseFragment {

	@FragmentArg
	Long messageId = 0L;

	@ViewById(R.id.messageText)
	TextView messageText;

	private Realm db;
	private Message message;

	public static MessageDetailFragment newInstance(Long messageId) {
		return MessageDetailFragment_.builder().messageId(messageId).build();
	}

	@AfterViews
	public void afterViews() {
		setAppbarUpNavigation(true);
		setAppbarTitle("Zpráva");
		db = RealmHelper.getDb();

		message = db.where(Message.class).equalTo("id", messageId).findFirst();
		if (message == null) {
			messageText.setText("Zpráva nenalezena...");
			return;
		}

		messageText.setText(message.getMessage());
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
