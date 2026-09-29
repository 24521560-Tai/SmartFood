package com.example.androidapp.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidapp.R;
import com.example.androidapp.api.ApiClient;
import com.example.androidapp.api.UserApi;
import com.example.androidapp.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private ImageView imgAvatar;
    private TextView txtGreeting;

    private UserApi userApi;


    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );


        // =========================
        // ÁNH XẠ VIEW
        // =========================

        imgAvatar = view.findViewById(R.id.imgAvatar);

        txtGreeting = view.findViewById(R.id.txtGreeting);


        // =========================
        // API
        // =========================

        userApi = ApiClient.getUserApi(requireContext());


        // =========================
        // LẤY THÔNG TIN USER
        // =========================

        loadCurrentUser();


        return view;
    }


    // =====================================================
    // LẤY THÔNG TIN USER HIỆN TẠI
    // =====================================================

    private void loadCurrentUser() {

        userApi.getCurrentUser().enqueue(
                new Callback<UserResponse>() {

                    @Override
                    public void onResponse(
                            Call<UserResponse> call,
                            Response<UserResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            UserResponse user = response.body();

                            // Hiển thị tên
                            txtGreeting.setText(
                                    "Xin chào, "
                                            + user.getUsername()
                                            + "!"
                            );

                            // Hiển thị avatar
                            setAvatar(user.getAvatar());

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Không thể tải thông tin người dùng"
                                            + "\nHTTP "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<UserResponse> call,
                            Throwable t) {

                        Toast.makeText(
                                requireContext(),
                                "Không thể kết nối server:\n"
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // =====================================================
    // HIỂN THỊ AVATAR
    // =====================================================

    private void setAvatar(String avatar) {

        if (avatar == null) {
            imgAvatar.setImageResource(
                    R.drawable.avatar_carrot
            );
            return;
        }

        switch (avatar) {

            case "avatar_broccoli":
                imgAvatar.setImageResource(
                        R.drawable.avatar_broccoli
                );
                break;

            case "avatar_tomato":
                imgAvatar.setImageResource(
                        R.drawable.avatar_tomato
                );
                break;

            case "avatar_cabbage":
                imgAvatar.setImageResource(
                        R.drawable.avatar_cabbage
                );
                break;

            case "avatar_corn":
                imgAvatar.setImageResource(
                        R.drawable.avatar_corn
                );
                break;

            case "avatar_avocado":
                imgAvatar.setImageResource(
                        R.drawable.avatar_avocado
                );
                break;

            case "avatar_orange":
                imgAvatar.setImageResource(
                        R.drawable.avatar_orange
                );
                break;

            case "avatar_chili":
                imgAvatar.setImageResource(
                        R.drawable.avatar_chili
                );
                break;

            case "avatar_carrot":
            default:
                imgAvatar.setImageResource(
                        R.drawable.avatar_carrot
                );
                break;
        }
    }
}