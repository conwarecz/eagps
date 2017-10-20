package net.aineuron.eagps.client;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.aineuron.eagps.BuildConfig;
import net.aineuron.eagps.Pref_;
import net.aineuron.eagps.R;
import net.aineuron.eagps.activity.LoginActivity_;
import net.aineuron.eagps.adapter.RealmStringListTypeAdapter;
import net.aineuron.eagps.client.client.EaClient;
import net.aineuron.eagps.client.client.EaClient_;
import net.aineuron.eagps.event.network.ApiErrorEvent;
import net.aineuron.eagps.event.network.KnownErrorEvent;
import net.aineuron.eagps.model.UserManager;
import net.aineuron.eagps.model.database.RealmString;
import net.aineuron.eagps.model.transfer.KnownError;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.realm.RealmList;
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
    public static final String END_POINT = "https://www.vgsdapi-test.europ-assistance.cz:41443/";

    public static final String END_POINT_OFICCIAL = "https://www.vgsdapi-test.europ-assistance.cz:41443/";
    public static final String END_POINT_TEST = "https://www.vgsdapi-preprod.europ-assistance.cz:41443/";

	@RootContext
	Context context;

	@Pref
	Pref_ pref;

    @Bean
    UserManager userManager;

	private Retrofit retrofit;
	private EaClient eaClient;
	private Gson gson;

	public static void postNetworkError(Throwable errorThrowable) {
		EventBus.getDefault().post(new ApiErrorEvent(errorThrowable));
	}

	public static void postKnownError(KnownError error) {
		EventBus.getDefault().post(new KnownErrorEvent(error));
	}

    public void postUnauthorisedError() {
        userManager.logout(userManager.getUser());
        Intent intent = new Intent(context, LoginActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Toast.makeText(context, "Přihlaste se znovu, prosím", Toast.LENGTH_LONG).show();
    }

	@AfterInject
	public void afterInject() {
		if (retrofit == null) {
			rebuildRetrofit();
		}
	}

    private OkHttpClient.Builder buildSslClient(OkHttpClient.Builder builder) throws Exception {
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = new BufferedInputStream(context
                .getResources().openRawResource(R.raw.coregpsapitest));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        // Tell the okhttp to use a SocketFactory from our SSLContext
        builder.sslSocketFactory(context.getSocketFactory()).build();
        return builder;
    }

	public void rebuildRetrofit() {
		String token = pref.token().get();

		gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd'T'HH:mm:sss")
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				.registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {
						}.getType(),
						RealmStringListTypeAdapter.INSTANCE)
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

        // Set token when not empty
        if (!token.isEmpty()) {
            clientBuilder.addInterceptor(chain -> {
				Request.Builder requestBuilder = chain.request().newBuilder();
                requestBuilder.addHeader("Authorization", "bearer " + token);
                Log.d("Authorisation header", "Bearer " + token);
                return chain.proceed(requestBuilder.build());
            });
		}

//		try {
//			clientBuilder = buildSslClient(clientBuilder);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

//		KeyStore keyStore = readKeyStore(); //your method to obtain KeyStore
//		SSLContext sslContext = SSLContext.getInstance("SSL");
//		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//		trustManagerFactory.init(keyStore);
//		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//		keyManagerFactory.init(keyStore, "keystore_pass".toCharArray());
//		sslContext.init(keyManagerFactory.getKeyManagers(),trustManagerFactory.getTrustManagers(), new SecureRandom());

        // TODO: remove on trusted server
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = new TrustManager[]{new TrustEveryoneManager()};
            sslContext.init(null, trustManagers, null);
            OkHttpClient client = clientBuilder
                    .sslSocketFactory(sslContext.getSocketFactory())
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    }).build();
            builder.client(client);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
//		OkHttpClient client = clientBuilder.build();
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

class TrustEveryoneManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}