package net.aineuron.eagps.model.viewmodel;

import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.ClientCar;
import net.aineuron.eagps.model.database.order.DestinationAddress;
import net.aineuron.eagps.model.database.order.Location;
import net.aineuron.eagps.model.database.order.Offer;

import org.androidannotations.annotations.EBean;

import java.util.Date;

/**
 * Created by Vit Veres on 05-Jun-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class OfferManager {

	public Offer getOfferById(Long id) {
		Offer offer = new Offer();

		ClientCar clientCar = new ClientCar();
		clientCar.setLicensePlate("4T8 4598");
		clientCar.setModel("Toyota Corolla");
		clientCar.setWeight(1.2);

		Location clientLocation = new Location();
		clientLocation.setLatitude(49.788892d);
		clientLocation.setLongitude(18.2440393d);

		Address clientAddress = new Address();
		clientAddress.setCity("Ostrava");
		clientAddress.setStreet("Plzenska 78");
		clientAddress.setZipCode("70030");
		clientAddress.setLocation(clientLocation);

		Location destionationLocation = new Location();
		destionationLocation.setLatitude(49.8359266d);
		destionationLocation.setLongitude(18.2668646d);

		Address destinationAddress = new Address();
		destinationAddress.setCity("Ostrava");
		destinationAddress.setStreet("U Stadiónu 3166/7");
		destinationAddress.setZipCode("70200");
		destinationAddress.setLocation(destionationLocation);

		DestinationAddress destinationAddressLoc = new DestinationAddress();
		destinationAddressLoc.setName("Best Drive");
		destinationAddressLoc.setAddress(destinationAddress);

		offer.setId(id);
		offer.setTime(new Date());
		offer.setCar(clientCar);
		offer.setClientAddress(clientAddress);
		offer.setDestinationAddress(destinationAddressLoc);
		offer.setClientName("Honza Prasil");
		offer.setClientPhone("+420 123 123 456");
		offer.setEventDescription("Odtah z kraje silnice, nefunkcni motor, nic nejede, kola dobre.");

		return offer;
	}
}
