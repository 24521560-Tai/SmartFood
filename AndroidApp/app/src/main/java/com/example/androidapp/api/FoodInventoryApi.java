package com.example.androidapp.api;
import retrofit2.http.Path;
import com.example.androidapp.model.FoodItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FoodInventoryApi {

    @GET("api/FoodItems")
    Call<List<FoodItemResponse>> getMyFoodItems();

    @GET("api/FoodItems/{id}")
    Call<FoodItemResponse> getFoodItemById(@Path("id") int id);
}