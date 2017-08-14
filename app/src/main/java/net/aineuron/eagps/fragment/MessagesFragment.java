package net.aineuron.eagps.fragment;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tmtron.greenannotations.EventBusGreenRobot;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.adapter.MessagesAdapter;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.ui.MessageClickedEvent;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Vit Veres on 19-Apr-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_messages)
public class MessagesFragment extends BaseFragment {

	@ViewById(R.id.messages)
	RecyclerView messagesView;

	@Bean
	ClientProvider clientProvider;

	@EventBusGreenRobot
	EventBus bus;

	private Realm db;
	private RealmResults<Message> messageRealmQuery;
	private MessagesAdapter adapter;

	public static MessagesFragment newInstance() {
		return MessagesFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(false);
		setAppbarTitle("Zprávy");

		db = RealmHelper.getDb();
		messageRealmQuery = db.where(Message.class).findAllSorted("time", Sort.DESCENDING);

		adapter = new MessagesAdapter(messageRealmQuery);

		LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
		DividerItemDecoration decor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.horizontal_divider_gray);
		decor.setDrawable(horizontalDivider);

		messagesView.setLayoutManager(manager);
		messagesView.addItemDecoration(decor);

		messagesView.setAdapter(adapter);

		clientProvider.getEaClient().updateMessages();
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageClickedEvent(MessageClickedEvent e) {
		MainActivityBase activity = (MainActivityBase) getActivity();
		activity.showFragment(MessageDetailFragment.newInstance(e.messageId));
	}
}
