package com.example.jobmatch_frontend.api;

import com.example.jobmatch_frontend.session.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    public static final String SERVER_URL =
            "http://192.168.68.52:5000";

    private static final String BASE_URL =
            SERVER_URL + "/api/";
    private static Retrofit retrofit = null;

    // Unauthenticated instance (Auth endpoints like Login/Register)
    public static AuthApi getService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client =
                    new OkHttpClient.Builder()
                            .connectTimeout(
                                    30,
                                    TimeUnit.SECONDS
                            )
                            .readTimeout(
                                    120,
                                    TimeUnit.SECONDS
                            )
                            .writeTimeout(
                                    120,
                                    TimeUnit.SECONDS
                            )
                            .addInterceptor(logging)
                            .build();


            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit.create(AuthApi.class);
    }

    // Authenticated instance (Includes Bearer Token via SessionManager)
    public static Retrofit getClient(SessionManager sessionManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();

                    if (sessionManager != null && sessionManager.getToken() != null) {
                        requestBuilder.header(
                                "Authorization",
                                "Bearer " + sessionManager.getToken()
                        );
                    }

                    requestBuilder.method(
                            original.method(),
                            original.body()
                    );

                    return chain.proceed(requestBuilder.build());
                })
                .build();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
}