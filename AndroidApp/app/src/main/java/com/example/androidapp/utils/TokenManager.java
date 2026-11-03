
package com.example.androidapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

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

    // Kiểm tra token có tồn tại và chưa hết hạn
    public boolean hasToken() {
        String token = getToken();

        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            // JWT gồm Header.Payload.Signature
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                clearToken();
                return false;
            }

            // Giải mã Payload
            byte[] decodedBytes = Base64.decode(
                    parts[1],
                    Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING
            );

            String payload = new String(
                    decodedBytes,
                    StandardCharsets.UTF_8
            );

            JSONObject json = new JSONObject(payload);

            // Lấy thời điểm hết hạn (Unix timestamp)
            long exp = json.getLong("exp");

            // Thời gian hiện tại tính bằng giây
            long currentTime = System.currentTimeMillis() / 1000;

            if (currentTime >= exp) {
                clearToken();
                return false;
            }

            return true;

        } catch (Exception e) {
            clearToken();
            return false;
        }
    }

    // Xóa JWT khi logout hoặc hết hạn
    public void clearToken() {
        preferences.edit()
                .remove(KEY_TOKEN)
                .apply();
    }
}
