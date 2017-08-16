package net.aineuron.eagps.client;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.aineuron.eagps.BuildConfig;
import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.client.client.EaClient;
import net.aineuron.eagps.client.client.EaClient_;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.model.transfer.KnownError;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Vit Veres on 19.2.2016
 * as a part of AlTraceabilitySystem project.
 */
@EBean(scope = EBean.Scope.Singleton)
public class ClientProvider {
	public static final String END_POINT = "http://eagpsapidev.azurewebsites.net/";

	@RootContext
	Context context;

	@Pref
	Pref_ pref;

	private Retrofit retrofit;
	private EaClient eaClient;
	private Gson gson;

	public static void postNetworkError(Throwable errorThrowable) {
		EventBus.getDefault().post(new ApiErrorEvent(errorThrowable));
	}

	public static void postKnownError(KnownError error) {
		EventBus.getDefault().post(new KnownErrorEvent(error));
	}

	@AfterInject
	public void afterInject() {
		if (retrofit == null) {
			rebuildRetrofit();
		}
	}

	public void rebuildRetrofit() {
		String token = pref.token().get();


		gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd'T'HH:mm:sss")
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				.create();

		Retrofit.Builder builder = new Retrofit.Builder()
				.baseUrl(END_POINT)
				.addConverterFactory(GsonConverterFactory.create(gson))
				.addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create());

		OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();

		if (BuildConfig.DEBUG) {
			HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
			interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			clientBuilder.addInterceptor(interceptor);
		}

		// Set token when empty
		if (!token.isEmpty()) {
			clientBuilder.addInterceptor(chain -> {
				Request.Builder requestBuilder = chain.request().newBuilder();
				requestBuilder.addHeader("Authorization", "Bearer " + token);
				return chain.proceed(requestBuilder.build());
			});
		}

		OkHttpClient client = clientBuilder.build();
		builder.client(client);
		retrofit = builder.build();

		initClients();
	}

	private void initClients() {
		eaClient = EaClient_.getInstance_(context).withRetrofit(retrofit);
	}

	public EaClient getEaClient() {
		return eaClient;
	}
}
