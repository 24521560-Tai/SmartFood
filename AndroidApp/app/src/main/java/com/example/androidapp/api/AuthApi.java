package com.example.androidapp.api;

import com.example.androidapp.model.LoginRequest;
import com.example.androidapp.model.LoginResponse;
import com.example.androidapp.model.RegisterRequest;
import com.example.androidapp.model.RegisterResponse;
import com.example.androidapp.model.ForgotPasswordRequest;
import com.example.androidapp.model.VerifyOtpRequest;
import com.example.androidapp.model.ResetPasswordRequest;
import com.example.androidapp.model.MessageResponse;
import com.example.androidapp.model.SetupProfileRequest;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
public interface AuthApi {

    @POST("api/Auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/Auth/register")
    Call<RegisterResponse> register(
            @Body RegisterRequest request
    );
    @POST("api/Auth/forgot-password")
    Call<MessageResponse> forgotPassword(
            @Body ForgotPasswordRequest request
    );

    @POST("api/Auth/verify-otp")
    Call<MessageResponse> verifyOtp(
            @Body VerifyOtpRequest request
    );

    @POST("api/Auth/reset-password")
    Call<MessageResponse> resetPassword(
            @Body ResetPasswordRequest request
    );

    @PUT("api/Auth/profile/{userId}")
    Call<MessageResponse> updateProfile(
            @Path("userId") int userId,
            @Body SetupProfileRequest request
    );
}