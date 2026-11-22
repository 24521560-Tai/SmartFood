
package com.example.androidapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.utils.TokenManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hiển thị giao diện Splash
        setContentView(R.layout.activity_splash);

        // Chờ 1,5 giây rồi chuyển màn hình
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

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

        }, 1500);
    }
}
