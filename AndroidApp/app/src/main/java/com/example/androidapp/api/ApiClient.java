package com.example.androidapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL =
            "http://10.0.251.76:5222/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static AuthApi getAuthApi() {

        return getRetrofit().create(AuthApi.class);
    }
}