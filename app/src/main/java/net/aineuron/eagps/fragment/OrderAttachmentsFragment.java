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
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.SelectionSpecBuilder;
import com.zhihu.matisse.engine.impl.GlideEngine;

import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.MainActivityBase;
import net.aineuron.eagps.adapter.PhotoPathsWithReasonAdapter;
import net.aineuron.eagps.adapter.PhotoPathsWithReasonAdapter_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.event.network.order.OrderSentEvent;
import net.aineuron.eagps.event.network.order.PhotoUploadedEvent;
import net.aineuron.eagps.event.network.order.SheetUploadedEvent;
import net.aineuron.eagps.event.ui.AddPhotoEvent;
import net.aineuron.eagps.event.ui.RemovePhotoEvent;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.database.RealmString;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.Photo;
import net.aineuron.eagps.model.database.order.PhotoPathsWithReason;
import net.aineuron.eagps.util.BitmapUtil;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.NetworkUtil;
import net.aineuron.eagps.util.RealmHelper;
import net.aineuron.eagps.view.widget.OrderDetailHeader;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import io.realm.ObjectChangeSet;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObjectChangeListener;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static net.aineuron.eagps.util.FileUtils.fileToBase64;

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

	@FragmentArg
	Long orderId;

	@Bean
	OrdersManager ordersManager;

	@Bean
	ClientProvider clientProvider;

	private Order order;
	private Realm db;
	private RealmObjectChangeListener objectListener;
	private int uploadedPhotos = 0;
	private int uploadedSheets = 0;

	public static OrderAttachmentsFragment newInstance(Long orderId) {
		return OrderAttachmentsFragment_.builder().orderId(orderId).build();
	}

	@Override
	public void onPause() {
		super.onPause();
		hideProgress();
	}

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(true);
		setAppbarTitle("Přílohy");

		if (orderId == null) {
			Toast.makeText(getContext(), "Načtena defaultní zakázka", Toast.LENGTH_LONG).show();
			order = ordersManager.getCurrentOrder();
			setContent();
		} else {
			order = ordersManager.getOrderById(orderId);
			setOrderListener();
			if (order == null) {
				if (NetworkUtil.isConnected(getContext())) {
					showProgress("Načítám detail", getString(R.string.dialog_wait_content));
				}
				clientProvider.getEaClient().getOrderDetail(orderId);
			} else {
				setContent();
			}
		}
	}

	@Click(R.id.closeOrder)
	public void closeOrder() {
		((MainActivityBase) getActivity()).showFragment(new OrdersFragment_());
	}

	@Click(R.id.sendOrder)
	public void sendOrder() {
		boolean isValid = true;

		if (order.getOrderDocuments().getPhotoPaths().size() == 0 && order.getOrderDocuments().getReasonForNoPhotos().isEmpty()) {
			isValid = false;
		}

		if (order.getPhotos().getPhotoPaths().size() == 0 && order.getPhotos().getReasonForNoPhotos().isEmpty()) {
			isValid = false;
		}

		if (!isValid) {
			new MaterialDialog.Builder(getContext())
					.title("Chyba")
					.content("Zakázku nelze odeslat, neboť nemáte přiřazeny fotografie, zakázkový list nebo není vyplněn důvod, proč tyto dokumenty nemůžete dodat.")
					.positiveText("Rozumím")
					.show();
			return;
		}

		showProgress("Odesílám zásah", getString(R.string.dialog_wait_content));
		if (order.getPhotos().getPhotoPaths().size() > 0 && uploadedPhotos < order.getPhotos().getPhotoPaths().size()) {
			uploadPhotos();
		} else if (order.getOrderDocuments().getPhotoPaths().size() > 0 && uploadedSheets < order.getOrderDocuments().getPhotoPaths().size()) {
			uploadSheets();
		} else {
			uploadFinishedSendOrder();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onOrderSentEvent(OrderSentEvent e) {
		hideProgress();
//		MainActivityBase activityBase = (MainActivityBase) getActivity();
//		activityBase.showFragment(new StateFragment(), false);
		IntentUtils.openMainActivity(getContext());
		getActivity().onBackPressed();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onAddPhotoClicked(AddPhotoEvent e) {
		if (e.TargetId == REQUEST_CODE_CHOOSE_DOCS) {
			OrderAttachmentsFragmentPermissionsDispatcher.showGalleryPickerForDocsWithCheck(this);
		} else {
			OrderAttachmentsFragmentPermissionsDispatcher.showGalleryPickerForPhotosWithCheck(this);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onRemovePhotoClicked(RemovePhotoEvent e) {
		PhotoPathsWithReason photoPathsWithReason = e.photoPathsWithReason;
		String photoPath = e.photoPath;

		for (Iterator<RealmString> iterator = photoPathsWithReason.getPhotoPaths().iterator(); iterator.hasNext(); ) {
			RealmString pp = iterator.next();
			if (pp.getValue().equalsIgnoreCase(photoPath)) {
				iterator.remove();
				setContent();
				return;
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onPhotoUploadedEvent(PhotoUploadedEvent e) {
		uploadedPhotos++;
		if (uploadedPhotos < order.getPhotos().getPhotoPaths().size()) {
			uploadPhotos();
		} else if (uploadedSheets < order.getOrderDocuments().getPhotoPaths().size()) {
			uploadSheets();
		} else {
			uploadFinishedSendOrder();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSheetUploadedEvent(SheetUploadedEvent e) {
		uploadedSheets++;
		if (uploadedSheets < order.getOrderDocuments().getPhotoPaths().size()) {
			uploadSheets();
		} else {
			uploadFinishedSendOrder();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkCarSelectedEvent(ApiErrorEvent e) {
		hideProgress();
		Toast.makeText(getContext(), e.throwable.getMessage(), Toast.LENGTH_SHORT).show();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCarSelectError(KnownErrorEvent e) {
		hideProgress();
		Toast.makeText(getContext(), e.knownError.getMessage(), Toast.LENGTH_SHORT).show();
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


			List<RealmString> paths;
			if (requestCode == REQUEST_CODE_CHOOSE_DOCS) {
				paths = order.getOrderDocuments().getPhotoPaths();
			} else {
				paths = order.getPhotos().getPhotoPaths();
			}

			for (Uri uri : mSelected) {
				if (db == null) {
					db = RealmHelper.getDb();
				}
				db.executeTransaction(realm -> paths.add(new RealmString(BitmapUtil.getRealPathFromUri(getContext(), uri))));
			}

			setContent();
		}
	}

	private void setContent() {
		orderDetailHeader.setContent(order, v -> {
			MainActivityBase activity = (MainActivityBase) getActivity();
			activity.showFragment(OrderDetailFragment.newInstance(order.getId(), null));
		});

		LinearLayoutManager horizontalManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		LinearLayoutManager horizontalManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

		DividerItemDecoration decor = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
		Drawable verticalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.vertical_divider);
		decor.setDrawable(verticalDivider);

		PhotoPathsWithReasonAdapter documents = null;
		if (order.getOrderDocuments() != null) {
			documents = PhotoPathsWithReasonAdapter_.getInstance_(getContext()).withPhotoPaths(order.getOrderDocuments()).withAddPhotoTargetId(REQUEST_CODE_CHOOSE_DOCS).finish();
			orderDocumentsView.setLayoutManager(horizontalManager);
			orderDocumentsView.addItemDecoration(decor);
			orderDocumentsView.setAdapter(documents);
		}

		PhotoPathsWithReasonAdapter photos = null;
		if (order.getPhotos() != null) {
			photos = PhotoPathsWithReasonAdapter_.getInstance_(getContext()).withPhotoPaths(order.getPhotos()).withAddPhotoTargetId(REQUEST_CODE_CHOOSE_PHOTOS).finish();
			orderPhotosView.setLayoutManager(horizontalManager2);
			orderPhotosView.addItemDecoration(decor);
			orderPhotosView.setAdapter(photos);
		}
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

	private void setOrderListener() {
		order = ordersManager.getOrderById(orderId);
		if (order != null) {
			objectListener = new RealmObjectChangeListener() {
				@Override
				public void onChange(RealmModel realmModel, ObjectChangeSet changeSet) {
					db = RealmHelper.getDb();
					order = ordersManager.getOrderById(orderId);
					if (orderDetailHeader != null) {
						setContent();
						hideProgress();
					}
				}
			};
			order.addChangeListener(objectListener);
		}
	}

	private void uploadPhotos() {
		String path = order.getPhotos().getPhotoPaths().get(uploadedPhotos).getValue();
		File file = new File(path);
		Photo photo = new Photo();
		try {
			photo.setExtension(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")));
			photo.setFileName(file.getName().substring(0, file.getName().lastIndexOf(".")));
			photo.setFileString(fileToBase64(file));
//			photo.setFileString(fileToByteArray2(file));
			clientProvider.getEaClient().uploadPhoto(photo, order.getId());
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("Photo file", "Couldn't create byte stream from file");
		}
	}

	private void uploadSheets() {
		String path = order.getOrderDocuments().getPhotoPaths().get(uploadedSheets).getValue();
		File file = new File(path);
		Photo photo = new Photo();
		try {
			photo.setExtension(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")));
			photo.setFileName(file.getName().substring(0, file.getName().lastIndexOf(".")));
			photo.setFileString(fileToBase64(file));
//			photo.setFileString(fileToByteArray2(file));
			clientProvider.getEaClient().uploadSheet(photo, order.getId());
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("Photo file", "Couldn't create byte stream from file");
		}
	}

	private void uploadFinishedSendOrder() {
		ordersManager.sendOrder(order.getId());
	}
}
