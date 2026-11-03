package com.example.androidapp.api;

import com.example.androidapp.model.AddFoodResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FoodItemApi {

    @Multipart
    @POST("api/FoodItems/manual")
    Call<AddFoodResponse> addManualFood(

            @Part("Name")
            RequestBody name,

            @Part("CategoryId")
            RequestBody categoryId,

            @Part("Quantity")
            RequestBody quantity,

            @Part("Unit")
            RequestBody unit,

            @Part("ExpiryDate")
            RequestBody expiryDate,

            @Part("Note")
            RequestBody note,

            @Part
            MultipartBody.Part image
    );
    @Multipart
    @POST("api/FoodItems/barcode")
    Call<AddFoodResponse> addBarcodeFood(
            @Part("Barcode") RequestBody barcode,
            @Part("Name") RequestBody name,
            @Part("Brand") RequestBody brand,
            @Part("CategoryId") RequestBody categoryId,
            @Part("Quantity") RequestBody quantity,
            @Part("Unit") RequestBody unit,
            @Part("ExpiryDate") RequestBody expiryDate,
            @Part("Note") RequestBody note,
            @Part MultipartBody.Part image
    );
}