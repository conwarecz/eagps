package net.aineuron.eagps.client.service;

import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.order.Order;

import java.util.List;

import io.reactivex.Maybe;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Created by Vit Veres on 31.3.2016
 * as a part of AlTraceabilitySystem project.
 */
public interface EaService {

	// User
	@GET("users/1/cars/available")
	Maybe<List<Car>> getCars();


	// Messages
	@GET("messages")
	Maybe<List<Message>> getMessages(@Query("skip") int skip, @Query("take") int take);

	@PUT("messages/{messageId}/read")
	Maybe<Void> setRead(long messageId, @Body boolean isRead);

	// Orders
	@GET("orders")
	Maybe<Order> getOrders(@Query("skip") int skip, @Query("take") int take);
}
