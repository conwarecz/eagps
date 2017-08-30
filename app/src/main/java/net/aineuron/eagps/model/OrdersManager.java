package net.aineuron.eagps.model;

import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.model.database.RealmString;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.AddressDetail;
import net.aineuron.eagps.model.database.order.DestinationAddress;
import net.aineuron.eagps.model.database.order.Limitation;
import net.aineuron.eagps.model.database.order.Location;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.util.RealmHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Vit Veres on 05-Jun-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class OrdersManager {

	private static Order order;

	@Pref
	Pref_ pref;

	@Bean
	ClientProvider clientProvider;

	public Order getCurrentOrder() {
		if (order != null) {
			return order;
		}
		order = new Order();

		order.setClientCarModel("Toyota Corolla");
		order.setClientCarWeight("1200 kg");
		order.setClientCarLicencePlate("4T8 4598");

		Location clientLocation = new Location();
		clientLocation.setLatitude(49.787973d);
		clientLocation.setLongitude(18.2285458d);

		Address clientAddress = new Address();
		AddressDetail clientAddressDetail = new AddressDetail();
		clientAddressDetail.setCity("Ostrava");
		clientAddressDetail.setStreet("Kosmonautů 2218/13");
		clientAddressDetail.setZipCode("70030");
		clientAddressDetail.setCountry("CZ");
		clientAddress.setAddress(clientAddressDetail);
		clientAddress.setLocation(clientLocation);

		Location destionationLocation = new Location();
		destionationLocation.setLatitude(49.8359266d);
		destionationLocation.setLongitude(18.2668646d);

		Address destinationAddress = new Address();
		AddressDetail destinationAddressDetail = new AddressDetail();

		destinationAddressDetail.setCity("Ostrava");
		destinationAddressDetail.setStreet("U Stadiónu 3166/7");
		destinationAddressDetail.setZipCode("70200");
		destinationAddressDetail.setCountry("CZ");
		destinationAddress.setAddress(destinationAddressDetail);
		destinationAddress.setLocation(destionationLocation);

		DestinationAddress destinationAddressLoc = new DestinationAddress();
		destinationAddressLoc.setName("Best Drive");
		destinationAddressLoc.setAddress(destinationAddress);

		Limitation limitation = new Limitation();
		limitation.setLimit("10 599 ,-");
		limitation.setExtendedDescription(false);

		order.setId(2L);
		order.setClaimSaxCode("T123456.78");
		order.setTimeCreated(new Date());
		order.setClientAddress(clientAddress);
        order.setDestinationAddress(destinationAddress);
        order.setLimitation(limitation);
        order.setClientFirstName("Honza");
		order.setClientLastName("Velký");
		order.setClientPhone("+420 123 123 456");
		order.setEventDescription(new RealmList<RealmString>(new RealmString("Odtah z kraje silnice"), new RealmString("nefunkcni motor"), new RealmString("nic nejede"), new RealmString("kola dobre")));

		return order;
	}

	public Order getOrderById(Long orderId) {
        Realm db = RealmHelper.getDb();
        Order order = db.where(Order.class).equalTo("id", orderId).findFirst();
        return order;
    }

	public void cancelOrder(Long orderId, Long reason) {
		clientProvider.getEaClient().cancelOrder(orderId, reason);
	}

	public void sendOrder(Long orderId) {
		clientProvider.getEaClient().sendOrder(orderId);
	}
}
