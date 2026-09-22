package com.example.androidapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.androidapp.R;
import com.example.androidapp.api.AuthApi;
import com.example.androidapp.api.ApiClient;
import com.example.androidapp.model.MessageResponse;
import com.example.androidapp.model.ResetPasswordRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText edtNewPassword;
    EditText edtConfirmPassword;
    Button btnResetPassword;

    AuthApi authApi;

    String email;
    String otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reset_password);

        // Ánh xạ View
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        // Lấy email và OTP từ VerifyOtpActivity
        email = getIntent().getStringExtra("email");
        otp = getIntent().getStringExtra("otp");

        // Lấy AuthApi
        authApi = ApiClient.getAuthApi();

        // Bấm nút đổi mật khẩu
        btnResetPassword.setOnClickListener(v -> {

            String newPassword = edtNewPassword
                    .getText()
                    .toString()
                    .trim();

            String confirmPassword = edtConfirmPassword
                    .getText()
                    .toString()
                    .trim();

            // Kiểm tra mật khẩu mới
            if (newPassword.isEmpty()) {

                Toast.makeText(
                        ResetPasswordActivity.this,
                        "Vui lòng nhập mật khẩu mới",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Kiểm tra nhập lại mật khẩu
            if (confirmPassword.isEmpty()) {

                Toast.makeText(
                        ResetPasswordActivity.this,
                        "Vui lòng nhập lại mật khẩu",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Kiểm tra hai mật khẩu có giống nhau không
            if (!newPassword.equals(confirmPassword)) {

                Toast.makeText(
                        ResetPasswordActivity.this,
                        "Mật khẩu nhập lại không khớp",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Tạo request
            ResetPasswordRequest request =
                    new ResetPasswordRequest(
                            email,
                            otp,
                            newPassword
                    );

            // Gọi API
            authApi.resetPassword(request)
                    .enqueue(new Callback<MessageResponse>() {

                        @Override
                        public void onResponse(
                                Call<MessageResponse> call,
                                Response<MessageResponse> response) {

                            if (response.isSuccessful()) {

                                Toast.makeText(
                                        ResetPasswordActivity.this,
                                        response.body().getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();

                                // Quay về màn hình Login
                                Intent intent = new Intent(
                                        ResetPasswordActivity.this,
                                        LoginActivity.class
                                );

                                // Xóa các Activity phía trước
                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_NEW_TASK
                                );

                                startActivity(intent);

                                finish();

                            } else {

                                Toast.makeText(
                                        ResetPasswordActivity.this,
                                        "Không thể đổi mật khẩu",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<MessageResponse> call,
                                Throwable t) {

                            Toast.makeText(
                                    ResetPasswordActivity.this,
                                    "Không thể kết nối đến server",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });
    }
}