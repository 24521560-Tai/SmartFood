package com.example.androidapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.api.ApiClient;
import com.example.androidapp.api.AuthApi;
import com.example.androidapp.model.RegisterRequest;
import com.example.androidapp.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail;
    EditText edtPassword;

    Button btnRegister;

    AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        btnRegister = findViewById(R.id.btnRegister);

        authApi = ApiClient.getAuthApi(this);

        btnRegister.setOnClickListener(v -> register());
    }


    private void register() {

        String email =
                edtEmail.getText().toString().trim();

        String password =
                edtPassword.getText().toString().trim();

        // Kiểm tra email
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            edtEmail.setError("Email không đúng định dạng");
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {
            edtPassword.setError(
                    "Password phải có ít nhất 8 ký tự"
            );
            edtPassword.requestFocus();
            return;
        }

        RegisterRequest request =
                new RegisterRequest(
                        email,
                        password
                );

        authApi.register(request)
                .enqueue(new Callback<RegisterResponse>() {

                    @Override
                    public void onResponse(
                            Call<RegisterResponse> call,
                            Response<RegisterResponse> response) {

                        if (response.isSuccessful()) {

                            RegisterResponse data = response.body();

                            if (data == null) {
                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Server không trả về dữ liệu",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            Toast.makeText(
                                    RegisterActivity.this,
                                    data.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();


                            // Chuyển sang màn hình nhập OTP
                            Intent intent =
                                    new Intent(
                                            RegisterActivity.this,
                                            VerifyOtpActivity.class
                                    );

                            // VerifyOtp cần biết email nào đang xác thực
                            intent.putExtra("email", email);

                            // Cho VerifyOtp biết đây là OTP đăng ký
                            intent.putExtra("purpose", "REGISTER");

                            startActivity(intent);

                            finish();

                        } else {

                            String error = "";

                            try {

                                if (response.errorBody() != null) {
                                    error =
                                            response.errorBody().string();
                                }

                            } catch (Exception e) {

                                error = e.getMessage();
                            }

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Đăng ký thất bại\n" + error,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<RegisterResponse> call,
                            Throwable t) {

                        Toast.makeText(
                                RegisterActivity.this,
                                "Không thể kết nối server:\n"
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}