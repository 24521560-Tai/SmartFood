package com.example.androidapp.api;

import com.example.androidapp.model.BarcodeResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BarcodeApi {

    @GET("api/Foods/barcode/{barcode}")
    Call<BarcodeResponse> getFoodByBarcode(
            @Path("barcode") String barcode
    );
}