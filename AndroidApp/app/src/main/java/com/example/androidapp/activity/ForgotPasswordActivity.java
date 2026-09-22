package com.example.androidapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.androidapp.R;
import com.example.androidapp.api.AuthApi;
import com.example.androidapp.api.ApiClient;
import com.example.androidapp.model.ForgotPasswordRequest;
import com.example.androidapp.model.MessageResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText edtForgotEmail;
    Button btnSendOtp;
    TextView txtBackToLogin;

    AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);

        // Ánh xạ View
        edtForgotEmail = findViewById(R.id.edtForgotEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);

        // Lấy AuthApi
        authApi = ApiClient.getAuthApi();

        // Bấm nút Gửi OTP
        btnSendOtp.setOnClickListener(v -> {

            String email = edtForgotEmail.getText()
                    .toString()
                    .trim();

            if (email.isEmpty()) {

                Toast.makeText(
                        ForgotPasswordActivity.this,
                        "Vui lòng nhập email",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            ForgotPasswordRequest request =
                    new ForgotPasswordRequest(email);

            authApi.forgotPassword(request)
                    .enqueue(new Callback<MessageResponse>() {

                        @Override
                        public void onResponse(
                                Call<MessageResponse> call,
                                Response<MessageResponse> response) {

                            if (response.isSuccessful()) {

                                Toast.makeText(
                                        ForgotPasswordActivity.this,
                                        response.body().getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();

                                // Chuyển sang màn hình nhập OTP
                                Intent intent = new Intent(
                                        ForgotPasswordActivity.this,
                                        VerifyOtpActivity.class
                                );

                                // Gửi email sang Activity OTP
                                intent.putExtra("email", email);

                                startActivity(intent);

                            } else {

                                Toast.makeText(
                                        ForgotPasswordActivity.this,
                                        "Email không tồn tại",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<MessageResponse> call,
                                Throwable t) {

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    "Không thể kết nối đến server",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        // Quay lại Login
        txtBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }
}