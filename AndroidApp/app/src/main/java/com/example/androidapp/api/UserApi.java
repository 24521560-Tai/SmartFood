package com.example.androidapp.api;

import com.example.androidapp.model.ChangePasswordRequest;
import com.example.androidapp.model.MessageResponse;
import com.example.androidapp.model.SetupProfileRequest;
import com.example.androidapp.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import com.example.androidapp.model.AvatarUploadResponse;

import okhttp3.MultipartBody;

import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

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
    @Multipart
    @POST("api/User/avatar")
    Call<AvatarUploadResponse> uploadAvatar(
            @Part MultipartBody.Part image
    );
}