package com.example.androidapp.model;

public class VerifyOtpRequest {

    private String email;
    private String otp;
    private String purpose;

    public VerifyOtpRequest(
            String email,
            String otp,
            String purpose) {

        this.email = email;
        this.otp = otp;
        this.purpose = purpose;
    }
}