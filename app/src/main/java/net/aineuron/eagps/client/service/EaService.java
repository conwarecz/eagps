package net.aineuron.eagps.client.service;

import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.transfer.LoginInfo;

import java.util.List;

import io.reactivex.Maybe;
import io.realm.RealmList;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Vit Veres on 31.3.2016
 * as a part of AlTraceabilitySystem project.
 */
public interface EaService {

	// User
    @POST("users/login")
    Maybe<User> login(@Body LoginInfo loginInfo);

    @POST("users/{userId}/logout")
    Maybe<Response<Void>> logout(@Path("userId") Long userId);

	@GET("users/{userId}/cars/available")
	Maybe<List<Car>> getCars(@Path("userId") Long userId);

    @PUT("entities/{entityId}/status")
    Maybe<Response<Void>> setStatus(@Path("entityId") long entityId, @Body Long status);

	@PUT("users/{userId}/entity/{entityId}")
	Maybe<Car> setCarToUser(@Path("userId") Long userId, @Path("entityId") long entityId);

    @DELETE("users/{userId}/car/{entityId}")
    Maybe<Response<Void>> releaseCarFromUser(@Path("userId") Long userId, @Path("entityId") long entityId);

	// Messages
	@GET("messages")
	Maybe<List<Message>> getMessages(@Query("skip") int skip, @Query("take") int take);

	@PUT("messages/{messageId}/read")
    Maybe<Response<Void>> setRead(@Path("messageId") long messageId, @Body Boolean isRead);

	// Orders
	@GET("orders")
	Maybe<RealmList<Order>> getOrders(@Query("skip") int skip, @Query("take") int take);

    @POST("orders/{orderId}/cancel")
    Maybe<Response<Void>> cancelOrder(@Path("orderId") Long orderId, @Body long reason);

    @POST("orders/{orderId}/finalize")
    Maybe<Response<Void>> finalizeOrder(@Path("orderId") Long orderId);

    @POST("orders/{orderId}/send")
    Maybe<Response<Void>> sendOrder(@Path("orderId") Long orderId);
}
