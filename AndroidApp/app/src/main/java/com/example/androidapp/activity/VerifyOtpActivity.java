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
import com.example.androidapp.model.MessageResponse;
import com.example.androidapp.model.VerifyOtpRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {

    EditText edtOtp;
    Button btnVerifyOtp;
    TextView txtBackToForgot;

    AuthApi authApi;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_verify_otp);

        // Ánh xạ View
        edtOtp = findViewById(R.id.edtOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        txtBackToForgot = findViewById(R.id.txtBackToForgot);

        // Lấy email từ ForgotPasswordActivity
        email = getIntent().getStringExtra("email");

        // Lấy AuthApi
        authApi = ApiClient.getAuthApi();

        // Xác nhận OTP
        btnVerifyOtp.setOnClickListener(v -> {

            String otp = edtOtp.getText()
                    .toString()
                    .trim();

            if (otp.isEmpty()) {

                Toast.makeText(
                        VerifyOtpActivity.this,
                        "Vui lòng nhập mã OTP",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (otp.length() != 6) {

                Toast.makeText(
                        VerifyOtpActivity.this,
                        "OTP phải có 6 chữ số",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            VerifyOtpRequest request =
                    new VerifyOtpRequest(email, otp);

            authApi.verifyOtp(request)
                    .enqueue(new Callback<MessageResponse>() {

                        @Override
                        public void onResponse(
                                Call<MessageResponse> call,
                                Response<MessageResponse> response) {

                            if (response.isSuccessful()) {

                                Toast.makeText(
                                        VerifyOtpActivity.this,
                                        response.body().getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();

                                // Chuyển sang màn hình đổi mật khẩu
                                Intent intent = new Intent(
                                        VerifyOtpActivity.this,
                                        ResetPasswordActivity.class
                                );

                                // Gửi email và OTP sang Activity tiếp theo
                                intent.putExtra("email", email);
                                intent.putExtra("otp", otp);

                                startActivity(intent);

                            } else {

                                Toast.makeText(
                                        VerifyOtpActivity.this,
                                        "OTP không đúng hoặc đã hết hạn",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<MessageResponse> call,
                                Throwable t) {

                            Toast.makeText(
                                    VerifyOtpActivity.this,
                                    "Không thể kết nối đến server",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        // Quay lại ForgotPasswordActivity
        txtBackToForgot.setOnClickListener(v -> {
            finish();
        });
    }
}