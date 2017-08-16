package net.aineuron.eagps.client.service;

import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.order.Order;

import java.util.List;

import io.reactivex.Maybe;
import io.realm.RealmList;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Vit Veres on 31.3.2016
 * as a part of AlTraceabilitySystem project.
 */
public interface EaService {

	// User
	@GET("users/{userId}/cars/available")
	Maybe<List<Car>> getCars(@Path("userId") Long userId);

	@PUT("users/{userId}/entity/{entityId}")
	Maybe<Car> setCarToUser(@Path("userId") Long userId, @Path("entityId") long entityId);

	// Messages
	@GET("messages")
	Maybe<List<Message>> getMessages(@Query("skip") int skip, @Query("take") int take);

	@PUT("messages/{messageId}/read")
	Maybe<Void> setRead(@Path("messageId") long messageId, @Body Boolean isRead);

	// Orders
	@GET("orders")
	Maybe<RealmList<Order>> getOrders(@Query("skip") int skip, @Query("take") int take);
}
