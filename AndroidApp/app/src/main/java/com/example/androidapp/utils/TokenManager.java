package com.example.androidapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREF_NAME = "smartfood_auth";
    private static final String KEY_TOKEN = "access_token";

    private final SharedPreferences preferences;

    public TokenManager(Context context) {
        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    // Lưu JWT
    public void saveToken(String token) {
        preferences.edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    // Lấy JWT
    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    // Kiểm tra có token hay chưa
    public boolean hasToken() {
        String token = getToken();

        return token != null && !token.isEmpty();
    }

    // Xóa token khi logout
    public void clearToken() {
        preferences.edit()
                .remove(KEY_TOKEN)
                .apply();
    }
}