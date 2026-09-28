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
import com.example.androidapp.model.VerifyOtpResponse;
import com.example.androidapp.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {

    EditText edtOtp;
    Button btnVerifyOtp;
    TextView txtBackToForgot;

    AuthApi authApi;

    String email;
    String purpose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_verify_otp);

        // Ánh xạ View
        edtOtp = findViewById(R.id.edtOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        txtBackToForgot = findViewById(R.id.txtBackToForgot);

        // Lấy dữ liệu từ Activity trước
        email = getIntent().getStringExtra("email");
        purpose = getIntent().getStringExtra("purpose");

        // Lấy AuthApi
        authApi = ApiClient.getAuthApi(this);

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
                    new VerifyOtpRequest(
                            email,
                            otp,
                            purpose
                    );

            authApi.verifyOtp(request)
                    .enqueue(new Callback<VerifyOtpResponse>() {

                        @Override
                        public void onResponse(
                                Call<VerifyOtpResponse> call,
                                Response<VerifyOtpResponse> response) {

                            if (response.isSuccessful()) {

                                VerifyOtpResponse data = response.body();

                                if (data == null) {
                                    Toast.makeText(
                                            VerifyOtpActivity.this,
                                            "Server không trả về dữ liệu",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                Toast.makeText(
                                        VerifyOtpActivity.this,
                                        data.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();

                                // OTP ĐĂNG KÝ
                                if ("REGISTER".equals(purpose)) {

                                    String token = data.getToken();
                                    if (token == null || token.isEmpty()) {

                                        Toast.makeText(
                                                VerifyOtpActivity.this,
                                                "Không nhận được token",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }
                                    TokenManager tokenManager =
                                            new TokenManager(VerifyOtpActivity.this);

                                    tokenManager.saveToken(token);
                                    Intent intent = new Intent(
                                            VerifyOtpActivity.this,
                                            SetupProfileActivity.class
                                    );

                                    startActivity(intent);
                                    finish();
                                }

                                // OTP QUÊN MẬT KHẨU
                                else if ("RESET_PASSWORD".equals(purpose)) {

                                    Intent intent = new Intent(
                                            VerifyOtpActivity.this,
                                            ResetPasswordActivity.class
                                    );

                                    intent.putExtra("email", email);
                                    intent.putExtra("otp", otp);

                                    startActivity(intent);
                                    finish();
                                }

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
                                Call<VerifyOtpResponse> call,
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