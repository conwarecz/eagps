package net.aineuron.eagps.model;

import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.client.ClientProvider;
import net.aineuron.eagps.model.database.order.Address;
import net.aineuron.eagps.model.database.order.ClientCar;
import net.aineuron.eagps.model.database.order.DestinationAddress;
import net.aineuron.eagps.model.database.order.Limitation;
import net.aineuron.eagps.model.database.order.Location;
import net.aineuron.eagps.model.database.order.Order;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Date;

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

		Limitation limitation = new Limitation();
		limitation.setLimit("10 599 ,-");
		limitation.setExtendedDescription(false);

		order.setId(2166l);
		order.setClaimNumber("T123456.78");
		order.setTime(new Date());
		order.setCar(clientCar);
		order.setClientAddress(clientAddress);
		order.setDestinationAddress(destinationAddressLoc);
		order.setLimitation(limitation);
		order.setClientName("Honza Velky");
		order.setClientPhone("+420 123 123 456");
		order.setEventDescription("Odtah z kraje silnice, nefunkcni motor, nic nejede, kola dobre.");

		return order;
	}

	public Order getOrderById(Long orderId) {
		return getCurrentOrder();
	}

	public void cancelOrder(Long orderId) {
		clientProvider.getEaClient().cancelOrder(orderId);
	}

	public void sendOrder(Long orderId) {
		clientProvider.getEaClient().sendOrder(orderId);
	}
}
