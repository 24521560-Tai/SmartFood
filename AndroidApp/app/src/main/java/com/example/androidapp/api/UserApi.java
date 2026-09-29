package com.example.androidapp.api;

import com.example.androidapp.model.ChangePasswordRequest;
import com.example.androidapp.model.MessageResponse;
import com.example.androidapp.model.SetupProfileRequest;
import com.example.androidapp.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface UserApi {

    @GET("api/User/me")
    Call<UserResponse> getCurrentUser();

    @PUT("api/User/profile")
    Call<MessageResponse> updateProfile(
            @Body SetupProfileRequest request
    );

    @PUT("api/User/change-password")
    Call<MessageResponse> changePassword(
            @Body ChangePasswordRequest request
    );
}