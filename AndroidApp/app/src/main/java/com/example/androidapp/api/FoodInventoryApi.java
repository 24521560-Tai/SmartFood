package com.example.androidapp.api;

import com.example.androidapp.model.FoodItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FoodInventoryApi {

    @GET("api/FoodItems")
    Call<List<FoodItemResponse>> getMyFoodItems();
}