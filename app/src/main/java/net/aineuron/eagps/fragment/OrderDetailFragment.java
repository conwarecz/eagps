package net.aineuron.eagps.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.aineuron.eagps.R;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.model.OrdersManager;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.FormatUtil;
import net.aineuron.eagps.util.IntentUtils;
import net.aineuron.eagps.util.NetworkUtil;
import net.aineuron.eagps.util.RealmHelper;
import net.aineuron.eagps.view.widget.IcoLabelTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import io.reactivex.annotations.Nullable;
import io.realm.ObjectChangeSet;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObjectChangeListener;

/**
 * Created by Vit Veres on 07-Jun-17
 * as a part of Android-EAGPS project.
 */

@EFragment(R.layout.fragment_order_detail)
public class OrderDetailFragment extends BaseFragment {

	@SystemService
	ClipboardManager clipboardManager;

	@ViewById(R.id.client)
	IcoLabelTextView client;
	@ViewById(R.id.clientCar)
	IcoLabelTextView clientCar;
	@ViewById(R.id.clientAddress)
	IcoLabelTextView clientAddress;
	@ViewById(R.id.destinationAddress)
	IcoLabelTextView destinationAddress;
	@ViewById(R.id.eventDescription)
	IcoLabelTextView eventDescription;
	@ViewById(R.id.limit)
	IcoLabelTextView limit;

	@ViewById(R.id.header)
	TextView header;

	@Nullable
	@FragmentArg
	Long orderId;

	@Nullable
	@FragmentArg
	String title;

	@Bean
	OrdersManager ordersManager;

	@Bean
	ClientProvider clientProvider;

	private Order order;
	private Realm db;
	private RealmObjectChangeListener objectListener;

	public static OrderDetailFragment newInstance(Long orderId, String title) {
		return OrderDetailFragment_.builder().orderId(orderId).title(title).build();
	}

	@AfterViews
	void afterViews() {
		if (title == null || title.isEmpty()) {
			setAppbarUpNavigation(true);
			setAppbarTitle("Detail");
		} else {
			setAppbarUpNavigation(false);
			setAppbarTitle(title);
		}

		if (orderId == null) {
//			Toast.makeText(getContext(), "Načtena defaultní zakázka", Toast.LENGTH_LONG).show();
//			this.order = ordersManager.gedDefaultOrder();
			Toast.makeText(getContext(), "Nastala chyba, prosím zkuste znovu", Toast.LENGTH_LONG).show();
			onBackClicked();
		} else {
			setOrderListener();
			if (NetworkUtil.isConnected(getContext())) {
				showProgress("Načítám detail", getString(R.string.dialog_wait_content));
			}
			clientProvider.getEaClient().getOrderDetail(orderId);
		}

		setUi();
	}

	@Click(R.id.client)
	void telephoneClicked() {
		IntentUtils.dialPhone(getContext(), order.getClientPhone());
	}

	@LongClick({R.id.client, R.id.clientAddress, R.id.destinationAddress, R.id.clientCar, R.id.eventDescription, R.id.limit})
	void clickedToCopy(IcoLabelTextView view) {
		String text = view.getText();
		copyToClipboard(text);
	}

	@Click(R.id.back)
	void onBackClicked() {
		getActivity().onBackPressed();
	}

	private void setUi() {

        if (order.getClaimSaxCode() != null) {
            this.header.setText("Detail objednávky " + order.getClaimSaxCode());
        }

        String client = "";
        if (order.getClientFirstName() != null) {
            client = client + order.getClientFirstName();
        }
        if (order.getClientLastName() != null) {
            if (client.length() > 0) {
                client = client + " ";
            }
            client = client + order.getClientLastName();
        }
        if (order.getClientPhone() != null) {
            if (client.length() > 0) {
                client = client + ", ";
            }
            client = client + order.getClientPhone();
        }
        this.client.setText(client);

        String clientCar = "";
        if (order.getClientCarModel() != null) {
            clientCar = clientCar + order.getClientCarModel();
        }
        if (order.getClientCarWeight() != null) {
            if (clientCar.length() > 0) {
                clientCar = clientCar + ", ";
            }
            clientCar = clientCar + order.getClientCarWeight();
        }
        if (order.getClientCarLicencePlate() != null) {
            if (clientCar.length() > 0) {
                clientCar = clientCar + ", ";
            }
            clientCar = clientCar + order.getClientCarLicencePlate();
        }
        this.clientCar.setText(clientCar);

		Address clientAddress = order.getClientAddress();
		if (clientAddress != null) {
			this.clientAddress.setText(formatClientAddress(clientAddress));
		}

		Address destinationAddress = order.getDestinationAddress();
		if (destinationAddress != null) {
			this.destinationAddress.setText(formatDestinationAddress(destinationAddress, order.getWorkshopName()));
			this.destinationAddress.setVisibility(View.VISIBLE);
		} else {
			this.destinationAddress.setVisibility(View.GONE);
		}

		if (order.getEventDescription() != null) {
			this.eventDescription.setText(FormatUtil.formatEvent(order.getEventDescription()));
		}

		if (order.getLimitation() != null && order.getLimitation().getLimit() != null) {
			this.limit.setText(order.getLimitation().getLimit());
		}
	}

	// Building up addresses from what we have
	@NonNull
	private String formatDestinationAddress(Address destinationAddress, String workshopName) {
		String addressResult = "";
		if (destinationAddress != null) {
			if (workshopName != null) {
				addressResult += workshopName;
			}
			if (destinationAddress.getAddress().getStreet() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getStreet();
			}
			if (destinationAddress.getAddress().getCity() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getCity();
			}
			if (destinationAddress.getAddress().getZipCode() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getZipCode();
			}
			this.destinationAddress.setText(addressResult);
		}
		return addressResult;
	}

	@NonNull
	private String formatClientAddress(Address clientAddress) {
		String addressResult = "";
		if (order.getClientAddress() != null) {
			if (clientAddress.getAddress().getStreet() != null) {
				addressResult += clientAddress.getAddress().getStreet();
			}
			if (clientAddress.getAddress().getCity() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += clientAddress.getAddress().getCity();
			}
			if (clientAddress.getAddress().getZipCode() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += clientAddress.getAddress().getZipCode();
			}
			this.clientAddress.setText(addressResult);
		}
		return addressResult;
	}

	private void copyToClipboard(String text) {
		ClipData clip = ClipData.newPlainText("Client Address", text);
		clipboardManager.setPrimaryClip(clip);
		Toast.makeText(getContext(), "Kopírováno: " + text, Toast.LENGTH_SHORT).show();
	}

	private void setOrderListener() {
		order = ordersManager.getOrderById(orderId);
		if (order != null) {
			objectListener = new RealmObjectChangeListener() {
				@Override
				public void onChange(RealmModel realmModel, ObjectChangeSet changeSet) {
					db = RealmHelper.getDb();
					order = ordersManager.getOrderById(orderId);
					if (header != null && order != null) {
						setUi();
						hideProgress();
					}
				}
			};
			order.addChangeListener(objectListener);
		}
	}
}
