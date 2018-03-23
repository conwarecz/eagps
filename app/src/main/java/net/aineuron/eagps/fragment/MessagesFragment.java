package net.aineuron.eagps.fragment;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.adapter.MessagesAdapter;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.event.ui.MessageClickedEvent;
import net.aineuron.eagps.event.ui.StopRefreshingEvent;
import net.aineuron.eagps.model.MessagesManager;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.transfer.Paging;
import net.aineuron.eagps.util.RealmHelper;
import net.aineuron.eagps.view.EndlessRecyclerViewScrollListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
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

	@ViewById(R.id.messagesSwipe)
	SwipeRefreshLayout swipeRefreshLayout;

	@Bean
	ClientProvider clientProvider;

	@Bean
	MessagesManager messagesManager;

	private Realm db;
	private RealmResults<Message> messageRealmQuery;
	private MessagesAdapter adapter;
	private Paging paging;

	public static MessagesFragment newInstance() {
		return MessagesFragment_.builder().build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(false);
		setAppbarTitle("Zprávy");
		paging = new Paging();

		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				paging = new Paging();
				clientProvider.getEaClient().updateMessages(paging);
			}
		});

		db = RealmHelper.getDb();
		messageRealmQuery = db.where(Message.class).sort("time", Sort.DESCENDING).findAll();

		adapter = new MessagesAdapter(messageRealmQuery);

		LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
		DividerItemDecoration decor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.horizontal_divider_gray);
		decor.setDrawable(horizontalDivider);

		messagesView.setLayoutManager(manager);
		messagesView.addItemDecoration(decor);

		messagesView.setAdapter(adapter);

		messagesView.addOnScrollListener(new EndlessRecyclerViewScrollListener(manager) {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				paging.nextPage();
				swipeRefreshLayout.setRefreshing(true);
				clientProvider.getEaClient().updateMessages(paging);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		swipeRefreshLayout.setRefreshing(true);
		clientProvider.getEaClient().updateMessages(paging);
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
		activity.showFragment(MessageDetailFragment_.newInstance(e.messageId));
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onStopRefreshing(StopRefreshingEvent e) {
		messagesManager.checkUnreadMessage();
		swipeRefreshLayout.setRefreshing(false);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void apiFailedEvent(ApiErrorEvent e) {
		swipeRefreshLayout.setRefreshing(false);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onKnownErrorEvent(KnownErrorEvent e) {
		swipeRefreshLayout.setRefreshing(false);
	}
}
