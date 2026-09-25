package com.example.androidapp.model;

public class SetupProfileRequest {

    private String username;
    private String avatar;

    public SetupProfileRequest(
            String username,
            String avatar
    ) {
        this.username = username;
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }
}