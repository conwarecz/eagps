package net.aineuron.eagps.model.viewmodel;

import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.ClientCar;
import net.aineuron.eagps.model.database.order.DestinationAddress;
import net.aineuron.eagps.model.database.order.Location;
import net.aineuron.eagps.model.database.order.Order;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Date;

/**
 * Created by Vit Veres on 05-Jun-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class OrdersManager {

	@Pref
	Pref_ pref;

	public Order getCurrentOrder() {
		Order order = new Order();

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

		order.setId(2166l);
		order.setClaimNumber("T123456.78");
		order.setTime(new Date());
		order.setCar(clientCar);
		order.setClientAddress(clientAddress);
		order.setDestinationAddress(destinationAddressLoc);
		order.setClientName("Honza Prasil");
		order.setClientPhone("+420 123 123 456");
		order.setEventDescription("Odtah z kraje silnice, nefunkcni motor, nic nejede, kola dobre.");

		return order;
	}
}
