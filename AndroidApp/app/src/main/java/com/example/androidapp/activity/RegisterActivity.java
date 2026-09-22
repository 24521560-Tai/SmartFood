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
import com.example.androidapp.model.RegisterRequest;
import com.example.androidapp.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText edtUsername;
    EditText edtEmail;
    EditText edtPassword;

    Button btnRegister;

    AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        btnRegister = findViewById(R.id.btnRegister);

        authApi = ApiClient.getAuthApi();

        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {

        String username =
                edtUsername.getText().toString().trim();

        String email =
                edtEmail.getText().toString().trim();

        String password =
                edtPassword.getText().toString().trim();

        if (username.isEmpty()) {
            edtUsername.setError("Vui lòng nhập username");
            edtUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        RegisterRequest request =
                new RegisterRequest(
                        username,
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

                            RegisterResponse data =
                                    response.body();

                            Toast.makeText(
                                    RegisterActivity.this,
                                    data.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Đăng ký thành công
                            // → quay về Login
                            Intent intent =
                                    new Intent(
                                            RegisterActivity.this,
                                            LoginActivity.class
                                    );

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