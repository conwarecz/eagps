package net.aineuron.eagps.model;

import net.aineuron.eagps.model.database.RealmString;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.AddressDetail;
import net.aineuron.eagps.model.database.order.DestinationAddress;
import net.aineuron.eagps.model.database.order.Limitation;
import net.aineuron.eagps.model.database.order.Location;
import net.aineuron.eagps.model.database.order.Offer;

import org.androidannotations.annotations.EBean;

import java.util.Date;

import io.realm.RealmList;

/**
 * Created by Vit Veres on 05-Jun-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class OfferManager {

	public Offer getOfferById(Long id) {
		Offer offer = new Offer();

		offer.setClientCarModel("Toyota Corolla");
		offer.setClientCarWeight("1200 kg");
		offer.setClientLicencePlate("4T8 4598");

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

		offer.setId(id);
		offer.setTimeCreated(new Date());
		offer.setClientAddress(clientAddress);
		offer.setDestinationAddress(destinationAddressLoc);
		offer.setLimitation(limitation);
		offer.setClientFirstName("Honza");
		offer.setClientLastName("Velký");
		offer.setClientPhone("+420 123 123 456");
		offer.setEventDescription(new RealmList<RealmString>(new RealmString("Odtah z kraje silnice"), new RealmString("nefunkcni motor"), new RealmString("nic nejede"), new RealmString("kola dobre")));

		return offer;
	}
}
