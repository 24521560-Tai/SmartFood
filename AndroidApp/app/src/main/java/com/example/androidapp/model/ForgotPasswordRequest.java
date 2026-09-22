package com.example.androidapp.model;

public class ForgotPasswordRequest {

    private String email;

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}