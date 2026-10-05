package com.example.androidapp.api;

import android.content.Context;

import okhttp3.OkHttpClient;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL =
            "http://10.0.147.155:5222/";

    private static Retrofit retrofit;


    public static Retrofit getRetrofit(Context context) {

        if (retrofit == null) {

            // Tạo Interceptor để tự động thêm JWT
            AuthInterceptor authInterceptor =
                    new AuthInterceptor(
                            context.getApplicationContext()
                    );


            // Tạo OkHttpClient
            OkHttpClient okHttpClient =
                    new OkHttpClient.Builder()
                            .addInterceptor(authInterceptor)
                            .build();


            // Tạo Retrofit
            retrofit =
                    new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(
                                    GsonConverterFactory.create()
                            )
                            .build();
        }

        return retrofit;
    }


    public static AuthApi getAuthApi(Context context) {

        return getRetrofit(context)
                .create(AuthApi.class);
    }
    public static UserApi getUserApi(Context context) {

        return getRetrofit(context)
                .create(UserApi.class);
    }
    public static FoodItemApi getFoodItemApi(Context context) {
        return getRetrofit(context).create(FoodItemApi.class);
    }
}