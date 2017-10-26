package net.aineuron.eagps.fragment;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.model.MessagesManager;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;

/**
 * Created by Vit Veres on 20-Jun-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_message_detail)
public class MessageDetailFragment extends BaseFragment {

	@Bean
	MessagesManager messagesManager;

	@Bean
	ClientProvider clientProvider;

	@FragmentArg
	Long messageId = 0L;

	@OptionsMenuItem(R.id.action_share)
	MenuItem menuShare;
	@OptionsMenuItem(R.id.action_copy)
	MenuItem menuCopy;

	@ViewById(R.id.messageText)
	TextView messageText;

	private Realm db;
	private Message message;

	public static MessageDetailFragment newInstance(Long messageId) {
		return MessageDetailFragment_.builder().messageId(messageId).build();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menuShare.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menuShare.setVisible(true);
		menuShare.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				IntentUtils.shareText(getContext(), message.getText());
				return false;
			}
		});
		menuCopy.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menuCopy.setVisible(true);
		menuCopy.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				IntentUtils.copyToClipboard(getContext(), message.getText());
				return false;
			}
		});
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

		messagesManager.setMessageRead(message.getId(), true);
		clientProvider.getEaClient().setMessageRead(message.getId(), true);

        if (message.getText() != null) {
            messageText.setText(message.getText());
        }
        messagesManager.checkUnreadMessage();
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
