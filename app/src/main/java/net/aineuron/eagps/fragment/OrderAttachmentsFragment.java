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
import net.aineuron.eagps.model.database.order.LocalPhotos;
import net.aineuron.eagps.model.database.order.LocalReasons;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.Photo;
import net.aineuron.eagps.model.database.order.PhotoFile;
import net.aineuron.eagps.model.database.order.Reasons;
import net.aineuron.eagps.util.BitmapUtil;
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
import java.util.ArrayList;
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

	@Bean
	PhotoPathsWithReasonAdapter documentsAdapter;

	@Bean
	PhotoPathsWithReasonAdapter photosAdapter;

	private Order order;
	private Realm db;
	private RealmObjectChangeListener objectListener;
	private int uploadedPhotos = 0;
	private LocalPhotos localPhotos;
	private LocalReasons localReasons;

	private boolean hasPhotos = false;
	private boolean hasDocuments = false;

	public static OrderAttachmentsFragment newInstance(Long orderId) {
		return OrderAttachmentsFragment_.builder().orderId(orderId).build();
	}

	@Override
    public void onPause() {
        hideProgress();

        checkReasons();

		saveReasonsToDb();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        localReasons = db.where(LocalReasons.class).equalTo("orderId", orderId).findFirst();
		if (localReasons != null && localReasons.isValid()) {
			setContent();
        }
    }

	@AfterViews
	void afterViews() {
		setAppbarUpNavigation(true);
		setAppbarTitle("Přílohy");

		if (db == null) {
			db = RealmHelper.getDb();
		}

		if (orderId == null) {
//			Toast.makeText(getContext(), "Načtena defaultní zakázka", Toast.LENGTH_LONG).show();
//			order = ordersManager.gedDefaultOrder();
//			setContent();
			Toast.makeText(getContext(), "Nastala chyba, prosím zkuste znovu", Toast.LENGTH_LONG).show();
			getActivity().onBackPressed();
		} else {
			localPhotos = db.where(LocalPhotos.class).equalTo("orderId", orderId).findFirst();
			localReasons = db.where(LocalReasons.class).equalTo("orderId", orderId).findFirst();

			if (localPhotos == null) {
				localPhotos = new LocalPhotos();
				localPhotos.setOrderId(orderId);
				db.executeTransaction(realm -> realm.copyToRealmOrUpdate(localPhotos));
				localPhotos = db.where(LocalPhotos.class).equalTo("orderId", orderId).findFirst();
			}

			order = ordersManager.getOrderById(orderId);
			if (order != null) {
				setContent();
			}
			if (NetworkUtil.isConnected(getContext())) {
				showProgress("Načítám detail", getString(R.string.dialog_wait_content));
			}
			setOrderListener();
			clientProvider.getEaClient().getOrderDetail(orderId);
		}
	}

	@Click(R.id.closeOrder)
	public void closeOrder() {
		checkReasons();
		((MainActivityBase) getActivity()).showFragment(new OrdersFragment_());
	}

	@Click(R.id.sendOrder)
	public void sendOrder() {
		checkReasons();

		if (!hasPhotos || !hasDocuments) {
			for (Photo photo : localPhotos.getLocalPhotos()) {
				if (photo.getType() == Photo.PHOTO_TYPE_PHOTO) {
					hasPhotos = true;
				} else if (photo.getType() == Photo.PHOTO_TYPE_DOCUMENT) {
					hasDocuments = true;
				}
			}
		}

		if (!hasPhotos || !hasDocuments) {
			new MaterialDialog.Builder(getContext())
					.title("Chyba")
					.content("Zakázku nelze odeslat, neboť nemáte přiřazeny fotografie, zakázkový list nebo není vyplněn důvod, proč tyto dokumenty nemůžete dodat.")
					.positiveText("Rozumím")
					.show();
			return;
		}

		if (localPhotos.isValid()) {
			showProgress("Odesílám zásah", getString(R.string.dialog_wait_content));
			if (localPhotos.getLocalPhotos().size() > 0 && uploadedPhotos < localPhotos.getLocalPhotos().size()) {
				uploadPhotos();
			} else {
				uploadFinishedSendOrder();
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onOrderSentEvent(OrderSentEvent e) {
		hideProgress();
		db.executeTransaction(realm -> {
			LocalPhotos photos = db.where(LocalPhotos.class).equalTo("orderId", orderId).findFirst();
			if (photos != null && photos.isValid()) {
				photos.deleteFromRealm();
			}
		});
		db.executeTransaction(realm -> {
			LocalReasons reasons = db.where(LocalReasons.class).equalTo("orderId", orderId).findFirst();
			if (reasons != null && reasons.isValid()) {
				reasons.deleteFromRealm();
			}
		});
		ordersManager.updateOrder(orderId);
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
		String photoPath = e.photoPath;
		for (Photo photo : localPhotos.getLocalPhotos()) {
			if (photo.getPath().equalsIgnoreCase(photoPath)) {
				db.executeTransaction(realm ->
						localPhotos.getLocalPhotos().remove(photo)
				);
				setContent();
				return;
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onPhotoUploadedEvent(PhotoUploadedEvent e) {
		uploadedPhotos++;
		if (uploadedPhotos < localPhotos.getLocalPhotos().size()) {
			uploadPhotos();
		} else {
			uploadFinishedSendOrder();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSheetUploadedEvent(SheetUploadedEvent e) {
		onPhotoUploadedEvent(new PhotoUploadedEvent());
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNetworkCarSelectedEvent(ApiErrorEvent e) {
		hideProgress();
        Toast.makeText(getContext(), e.message, Toast.LENGTH_SHORT).show();
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

			if (db == null && mSelected.size() > 0) {
				db = RealmHelper.getDb();
			}

			for (Uri uri : mSelected) {
				Photo photo = new Photo();
				photo.setPath(BitmapUtil.getRealPathFromUri(getContext(), uri));
				if (requestCode == REQUEST_CODE_CHOOSE_DOCS) {
					photo.setType(Photo.PHOTO_TYPE_DOCUMENT);
				} else {
					photo.setType(Photo.PHOTO_TYPE_PHOTO);
				}
				db.executeTransaction(realm -> localPhotos.getLocalPhotos().add(photo));
			}

			setContent();
		}
	}

	private void setContent() {
		orderDetailHeader.setContent(order, v -> {
			MainActivityBase activity = (MainActivityBase) getActivity();
			activity.showFragment(OrderDetailFragment.newInstance(order.getId(), null));
		});

		LinearLayoutManager documentsLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		LinearLayoutManager photosLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

		DividerItemDecoration decor = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
		Drawable verticalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.vertical_divider);
		decor.setDrawable(verticalDivider);

		List<Photo> photos = new ArrayList<>();
		List<Photo> documents = new ArrayList<>();

		if (localPhotos != null && localPhotos.isValid() && localPhotos.getLocalPhotos() != null) {
			for (Photo photo : localPhotos.getLocalPhotos()) {
				if (photo.getType() == Photo.PHOTO_TYPE_PHOTO) {
					photos.add(photo);
				} else if (photo.getType() == Photo.PHOTO_TYPE_DOCUMENT) {
					documents.add(photo);
				}
			}
		}

		String photoReason = order.getReasonForNoPhotos();
		if (localReasons != null && (photoReason == null || photoReason.isEmpty())) {
			photoReason = localReasons.getReasons().getReasonForNoPhotos();
		}
		String docsReason = order.getReasonForNoDocuments();
		if (localReasons != null && (docsReason == null || docsReason.isEmpty())) {
			docsReason = localReasons.getReasons().getReasonForNoDocuments();
		}

		if (localPhotos.getLocalPhotos() != null) {
			documentsAdapter = PhotoPathsWithReasonAdapter_.getInstance_(getContext())
					.withPhotoPaths(documents)
					.withAddPhotoTargetId(REQUEST_CODE_CHOOSE_DOCS)
					.withReason(docsReason)
					.finish();
			orderDocumentsView.setLayoutManager(documentsLayoutManager);
			orderDocumentsView.addItemDecoration(decor);
			orderDocumentsView.setAdapter(documentsAdapter);
		}

		if (localPhotos.getLocalPhotos() != null) {
			photosAdapter = PhotoPathsWithReasonAdapter_.getInstance_(getContext())
					.withPhotoPaths(photos)
					.withAddPhotoTargetId(REQUEST_CODE_CHOOSE_PHOTOS)
					.withReason(photoReason)
					.finish();
			orderPhotosView.setLayoutManager(photosLayoutManager);
			orderPhotosView.addItemDecoration(decor);
			orderPhotosView.setAdapter(photosAdapter);
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
		String path = localPhotos.getLocalPhotos().get(uploadedPhotos).getPath();
		File file = new File(path);
		PhotoFile photoFile = new PhotoFile();
		try {
			photoFile.setExtension(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")));
			photoFile.setFileName(file.getName().substring(0, file.getName().lastIndexOf(".")));
			photoFile.setFileString(fileToBase64(file));
//			photoFile.setFileString(fileToByteArray2(file));
			if (localPhotos.getLocalPhotos().get(uploadedPhotos).getType() == Photo.PHOTO_TYPE_PHOTO) {
				clientProvider.getEaClient().uploadPhoto(photoFile, order.getId());
			} else if (localPhotos.getLocalPhotos().get(uploadedPhotos).getType() == Photo.PHOTO_TYPE_DOCUMENT) {
				clientProvider.getEaClient().uploadSheet(photoFile, order.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("PhotoFile file", "Couldn't create byte stream from file");
		}
	}

	private void uploadFinishedSendOrder() {
		saveReasonsToDb();
		localReasons = db.where(LocalReasons.class).equalTo("orderId", orderId).findFirst();
		if (localReasons == null) {
			Reasons reasons = new Reasons();
			localReasons = new LocalReasons();
			localReasons.setReasons(reasons);
			localReasons.setOrderId(orderId);
		}
		ordersManager.sendOrder(order.getId(), localReasons.getReasons());
	}

	private void checkReasons() {
		String noPhotosReason = photosAdapter.getReason();
		String noDocumentsReason = documentsAdapter.getReason();

		if (noPhotosReason != null && !noPhotosReason.isEmpty()) {
			db.executeTransaction(realm -> {
				order.setReasonForNoPhotos(noPhotosReason);
			});
			hasPhotos = true;
		}

		if (noDocumentsReason != null && !noDocumentsReason.isEmpty()) {
			db.executeTransaction(realm -> {
				order.setReasonForNoDocuments(noDocumentsReason);
			});
			hasDocuments = true;
		}
	}

	private void saveReasonsToDb() {
		boolean saveToDb = false;
		LocalReasons localReasons = new LocalReasons();
		Reasons reasons = new Reasons();
		localReasons.setOrderId(orderId);
		localReasons.setReasons(reasons);
		String documentReason = order.getReasonForNoDocuments();
		if (documentReason != null && !documentReason.isEmpty()) {
			localReasons.getReasons().setReasonForNoDocuments(documentReason);
			saveToDb = true;
		}
		String photoReason = order.getReasonForNoPhotos();
		if (photoReason != null && !photoReason.isEmpty()) {
			localReasons.getReasons().setReasonForNoPhotos(photoReason);
			saveToDb = true;
		}
		if (saveToDb) {
			db.executeTransaction(realm -> realm.copyToRealmOrUpdate(localReasons));
		}
	}
}
