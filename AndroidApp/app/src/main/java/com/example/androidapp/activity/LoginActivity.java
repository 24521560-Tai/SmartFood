package com.example.androidapp.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.androidapp.R;
import com.example.androidapp.api.AuthApi;
import com.example.androidapp.api.ApiClient;
import com.example.androidapp.model.LoginRequest;
import com.example.androidapp.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail;
    EditText edtPassword;

    Button btnLogin;
    TextView txtRegister;
    TextView txtForgotPassword;
    AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Ánh xạ View
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);

        // Lấy API
        authApi = ApiClient.getAuthApi();

        // Bắt sự kiện nút Login
        btnLogin.setOnClickListener(v -> login());

        // Bấm đăng ký
        txtRegister.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            RegisterActivity.class
                    );

            startActivity(intent);

        });
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        txtForgotPassword.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    ForgotPasswordActivity.class
            );

            startActivity(intent);
        });
    }

    private void login() {

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Kiểm tra email
        if (email.isEmpty()) {

            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();

            return;
        }

        // Kiểm tra password
        if (password.isEmpty()) {

            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();

            return;
        }

        // Tạo request
        LoginRequest request =
                new LoginRequest(email, password);

        // Gọi API
        authApi.login(request).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(
                    Call<LoginResponse> call,
                    Response<LoginResponse> response) {

                if (response.isSuccessful()) {

                    LoginResponse data = response.body();

                    Intent intent =
                            new Intent(
                                    LoginActivity.this,
                                    SetupProfileActivity.class
                            );

                    intent.putExtra("username", data.getUsername());
                    intent.putExtra("email", data.getEmail());

                    startActivity(intent);

                    finish();

                }
                else {

                    String error = "";

                    try {
                        if (response.errorBody() != null) {
                            error = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        error = e.getMessage();
                    }

                    Toast.makeText(
                            LoginActivity.this,
                            "HTTP " + response.code() + "\n" + error,
                            Toast.LENGTH_LONG
                    ).show();
                }}

            @Override
            public void onFailure(
                    Call<LoginResponse> call,
                    Throwable t) {

                Toast.makeText(
                        LoginActivity.this,
                        "Không thể kết nối tới server: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}