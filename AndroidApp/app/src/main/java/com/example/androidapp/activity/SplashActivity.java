package com.example.androidapp.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.utils.TokenManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TokenManager tokenManager =
                new TokenManager(this);

        Intent intent;

        if (tokenManager.hasToken()) {

            // Đã có JWT
            intent = new Intent(
                    SplashActivity.this,
                    HomeActivity.class
            );

        } else {

            // Chưa đăng nhập
            intent = new Intent(
                    SplashActivity.this,
                    LoginActivity.class
            );
        }

        startActivity(intent);
        finish();
    }
}