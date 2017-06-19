package net.aineuron.eagps.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.tmtron.greenannotations.EventBusGreenRobot;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.SelectionSpecBuilder;
import com.zhihu.matisse.engine.impl.GlideEngine;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.activity.MainActivity_;
import net.aineuron.eagps.adapter.PhotoPathsWithReasonAdapter;
import net.aineuron.eagps.adapter.PhotoPathsWithReasonAdapter_;
import net.aineuron.eagps.event.network.order.OrderSentEvent;
import net.aineuron.eagps.event.ui.AddPhotoEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.BitmapUtil;
import net.aineuron.eagps.view.widget.OrderDetailHeader;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by Vit Veres on 07-Jun-17
 * as a part of Android-EAGPS project.
 */

@RuntimePermissions
@EFragment(R.layout.fragnent_order_attachments)
public class OrderAttachmentsFragment extends BaseFragment {
	public static final int REQUEST_CODE_CHOOSE_DOCS = 8973;
	public static final int REQUEST_CODE_CHOOSE_PHOTOS = 8975;

	@ViewById(R.id.orderDetailHeader)
	OrderDetailHeader orderDetailHeader;

	@ViewById(R.id.orderDocumentsView)
	RecyclerView orderDocumentsView;

	@ViewById(R.id.orderPhotosView)
	RecyclerView orderPhotosView;

	@EventBusGreenRobot
	EventBus bus;

	@FragmentArg
	Long orderId;

	@Bean
	OrdersManager ordersManager;

	private Order order;

	public static OrderAttachmentsFragment newInstance(Long orderId) {
		return OrderAttachmentsFragment_.builder().orderId(orderId).build();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(true);
		setAppbarTitle("Přílohy");

		order = ordersManager.getOrderById(orderId);

		setContent();
	}

	@Click(R.id.closeOrder)
	public void closeOrder() {
		getActivity().onBackPressed();
	}

	@Click(R.id.sendOrder)
	public void sendOrder() {
		showProgress("Odesílám zásah", "Prosím čekejte...");
		ordersManager.sendOrder(order.getId());
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onOrderSentEvent(OrderSentEvent e) {
		hideProgress();
		MainActivity_.intent(getContext()).start();
		getActivity().finish();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onAddPhotoClicked(AddPhotoEvent e) {
		if (e.TargetId == REQUEST_CODE_CHOOSE_DOCS) {
			OrderAttachmentsFragmentPermissionsDispatcher.showGalleryPickerForDocsWithCheck(this);
		} else {
			OrderAttachmentsFragmentPermissionsDispatcher.showGalleryPickerForPhotosWithCheck(this);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		OrderAttachmentsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

	@NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
	protected void showGalleryPickerForDocs() {
		getMattise().forResult(REQUEST_CODE_CHOOSE_DOCS);
	}

	@NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
	protected void showGalleryPickerForPhotos() {
		getMattise().forResult(REQUEST_CODE_CHOOSE_PHOTOS);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == AppCompatActivity.RESULT_OK) {
			List<Uri> mSelected = Matisse.obtainResult(data);
			Log.d("Matisse", "mSelected: " + mSelected);


			List<String> paths;
			if (requestCode == REQUEST_CODE_CHOOSE_DOCS) {
				paths = order.getOrderDocuments().getPhotoPaths();
			} else {
				paths = order.getPhotos().getPhotoPaths();
			}

			for (Uri uri : mSelected) {
				paths.add(BitmapUtil.getRealPathFromUri(getContext(), uri));
			}

			setContent();
		}
	}

	private void setContent() {
		orderDetailHeader.setContent(order, v -> {
			MainActivityBase activity = (MainActivityBase) getActivity();
			activity.showFragment(OrderDetailFragment.newInstance());
		});

		PhotoPathsWithReasonAdapter documents = PhotoPathsWithReasonAdapter_.getInstance_(getContext()).withPhotoPaths(order.getOrderDocuments()).withAddPhotoTargetId(REQUEST_CODE_CHOOSE_DOCS).finish();
		PhotoPathsWithReasonAdapter photos = PhotoPathsWithReasonAdapter_.getInstance_(getContext()).withPhotoPaths(order.getPhotos()).withAddPhotoTargetId(REQUEST_CODE_CHOOSE_PHOTOS).finish();

		LinearLayoutManager horizontalManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		LinearLayoutManager horizontalManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

		orderDocumentsView.setLayoutManager(horizontalManager);
		orderPhotosView.setLayoutManager(horizontalManager2);

		DividerItemDecoration decor = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
		Drawable verticalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.vertical_divider);
		decor.setDrawable(verticalDivider);

		orderDocumentsView.addItemDecoration(decor);
		orderPhotosView.addItemDecoration(decor);

		orderDocumentsView.setAdapter(documents);
		orderPhotosView.setAdapter(photos);
	}

	private SelectionSpecBuilder getMattise() {
		return Matisse.from(this)
				.choose(MimeType.allOf())
				.countable(true)
				.maxSelectable(256)
				.restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
				.thumbnailScale(0.85f)
				.imageEngine(new GlideEngine());
	}
}
