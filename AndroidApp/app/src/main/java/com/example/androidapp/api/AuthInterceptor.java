package com.example.androidapp.api;
import android.content.Context;
import com.example.androidapp.utils.TokenManager;
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;

    public AuthInterceptor(Context context) {

        tokenManager = new TokenManager(context);
    }

    @Override
    public Response intercept(Chain chain)
            throws IOException {

        Request originalRequest = chain.request();

        String token = tokenManager.getToken();

        // Chưa đăng nhập -> gửi request bình thường
        if (token == null || token.isEmpty()) {

            return chain.proceed(originalRequest);
        }

        // Có JWT -> thêm Authorization Header
        Request newRequest =
                originalRequest
                        .newBuilder()
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .build();
        return chain.proceed(newRequest);
    }
}