package net.aineuron.eagps.client.service;

import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.User;
import net.aineuron.eagps.model.database.order.Order;
import net.aineuron.eagps.model.database.order.PhotoFile;
import net.aineuron.eagps.model.database.order.ReasonsRequestBody;
import net.aineuron.eagps.model.transfer.LoginInfo;
import net.aineuron.eagps.model.transfer.tender.TenderAcceptModel;
import net.aineuron.eagps.model.transfer.tender.TenderRejectModel;

import java.util.List;

import io.reactivex.Maybe;
import io.realm.RealmList;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
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
    Maybe<Response<User>> login(@Header("Version") String version, @Body LoginInfo loginInfo);

    @POST("users/{userId}/logout")
    Maybe<Response<Void>> logout(@Path("userId") Long userId);

    @GET("users/{userId}")
    Maybe<User> getUser(@Path("userId") Long userId);

	@GET("users/{userId}/cars/available")
	Maybe<List<Car>> getCars(@Path("userId") Long userId);

	@PUT("users/{userId}/entity/{entityId}")
	Maybe<Car> setCarToUser(@Path("userId") Long userId, @Path("entityId") long entityId);

    @DELETE("users/{userId}/entity/{entityId}")
    Maybe<Response<Void>> releaseCarFromUser(@Path("userId") Long userId, @Path("entityId") long entityId);

    @PUT("users/{userId}/token")
    Maybe<Response<Void>> setToken(@Path("userId") Long userId, @Body String token);

	// Messages
	@GET("messages")
	Maybe<List<Message>> getMessages(@Query("skip") int skip, @Query("take") int take);

	@PUT("messages/{messageId}/read")
    Maybe<Response<Void>> setRead(@Path("messageId") long messageId, @Body Boolean isRead);

	// Orders
	@GET("orders")
	Maybe<RealmList<Order>> getOrders(@Query("skip") int skip, @Query("take") int take);

    @GET("orders/{orderId}")
    Maybe<Order> getOrderDetail(@Path("orderId") Long orderId);

    @POST("orders/{orderId}/cancel")
    Maybe<Response<Void>> cancelOrder(@Path("orderId") Long orderId, @Body long reason);

    @POST("orders/{orderId}/finalize")
    Maybe<Response<Void>> finalizeOrder(@Path("orderId") Long orderId);

    @POST("orders/{orderId}/send")
    Maybe<Response<Void>> sendOrder(@Path("orderId") Long orderId, @Body ReasonsRequestBody reasons);

    // Photos
    @POST("orders/{orderId}/photos")
    Maybe<Response<Void>> uploadPhoto(@Path("orderId") Long orderId, @Body PhotoFile photoFile);

    @POST("orders/{orderId}/ordersheets")
    Maybe<Response<Void>> uploadSheet(@Path("orderId") Long orderId, @Body PhotoFile photoFile);

    // Cars
    @PUT("entities/{entityId}/status")
    Maybe<Response<Void>> setStatus(@Path("entityId") long entityId, @Body Long status);

    // Tenders
    @PUT("tenders/{tenderId}/accept")
    Maybe<Response<Void>> acceptTender(@Path("tenderId") long tenderId, @Body TenderAcceptModel tenderModel);

    @PUT("tenders/{tenderId}/reject")
    Maybe<Response<Void>> rejectTender(@Path("tenderId") long tenderId, @Body TenderRejectModel tenderModel);

    // Tests
    @POST("test/badrequest/{errorCode}")
    Maybe<Response<Void>> testErrorResponse(@Path("errorCode") Long errorCode);
}
