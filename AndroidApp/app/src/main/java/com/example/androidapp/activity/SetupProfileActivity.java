package com.example.androidapp.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.api.ApiClient;
import com.example.androidapp.api.AuthApi;
import com.example.androidapp.model.MessageResponse;
import com.example.androidapp.model.SetupProfileRequest;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetupProfileActivity extends AppCompatActivity {

    // =========================
    // USER ID
    // =========================

    private int userId;


    // =========================
    // INPUT
    // =========================

    private TextInputEditText edtDisplayName;


    // =========================
    // AVATAR HIỆN TẠI
    // =========================

    private android.widget.ImageView imgSelectedAvatar;


    // =========================
    // AVATAR CARD
    // =========================

    private MaterialCardView avatarCard1;
    private MaterialCardView avatarCard2;
    private MaterialCardView avatarCard3;
    private MaterialCardView avatarCard4;
    private MaterialCardView avatarCard5;
    private MaterialCardView avatarCard6;
    private MaterialCardView avatarCard7;
    private MaterialCardView avatarCard8;


    // =========================
    // BUTTON
    // =========================

    private Button btnStart;


    // =========================
    // API
    // =========================

    private AuthApi authApi;


    // =========================
    // AVATAR ĐANG CHỌN
    // =========================

    private String selectedAvatar = "avatar_carrot";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup_profile);


        // =========================
        // NHẬN USER ID
        // =========================

        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {

            Toast.makeText(
                    this,
                    "Không tìm thấy thông tin tài khoản.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }


        // =========================
        // ÁNH XẠ VIEW
        // =========================

        edtDisplayName =
                findViewById(R.id.edtDisplayName);

        imgSelectedAvatar =
                findViewById(R.id.imgSelectedAvatar);


        avatarCard1 =
                findViewById(R.id.avatarCard1);

        avatarCard2 =
                findViewById(R.id.avatarCard2);

        avatarCard3 =
                findViewById(R.id.avatarCard3);

        avatarCard4 =
                findViewById(R.id.avatarCard4);

        avatarCard5 =
                findViewById(R.id.avatarCard5);

        avatarCard6 =
                findViewById(R.id.avatarCard6);

        avatarCard7 =
                findViewById(R.id.avatarCard7);

        avatarCard8 =
                findViewById(R.id.avatarCard8);


        btnStart =
                findViewById(R.id.btnStart);


        // =========================
        // API
        // =========================

        authApi = ApiClient.getAuthApi();


        // =========================
        // CHỌN AVATAR
        // =========================

        avatarCard1.setOnClickListener(v ->
                selectAvatar(
                        avatarCard1,
                        "avatar_carrot",
                        R.drawable.avatar_carrot
                )
        );

        avatarCard2.setOnClickListener(v ->
                selectAvatar(
                        avatarCard2,
                        "avatar_broccoli",
                        R.drawable.avatar_broccoli
                )
        );

        avatarCard3.setOnClickListener(v ->
                selectAvatar(
                        avatarCard3,
                        "avatar_tomato",
                        R.drawable.avatar_tomato
                )
        );

        avatarCard4.setOnClickListener(v ->
                selectAvatar(
                        avatarCard4,
                        "avatar_cabbage",
                        R.drawable.avatar_cabbage
                )
        );

        avatarCard5.setOnClickListener(v ->
                selectAvatar(
                        avatarCard5,
                        "avatar_corn",
                        R.drawable.avatar_corn
                )
        );

        avatarCard6.setOnClickListener(v ->
                selectAvatar(
                        avatarCard6,
                        "avatar_avocado",
                        R.drawable.avatar_avocado
                )
        );

        avatarCard7.setOnClickListener(v ->
                selectAvatar(
                        avatarCard7,
                        "avatar_orange",
                        R.drawable.avatar_orange
                )
        );

        avatarCard8.setOnClickListener(v ->
                selectAvatar(
                        avatarCard8,
                        "avatar_chili",
                        R.drawable.avatar_chili
                )
        );


        // =========================
        // AVATAR MẶC ĐỊNH
        // =========================

        selectAvatar(
                avatarCard1,
                "avatar_carrot",
                R.drawable.avatar_carrot
        );


        // =========================
        // BUTTON BẮT ĐẦU
        // =========================

        btnStart.setOnClickListener(v ->
                updateProfile()
        );
    }


    // =====================================================
    // CHỌN AVATAR
    // =====================================================

    private void selectAvatar(
            MaterialCardView selectedCard,
            String avatarName,
            int avatarResource
    ) {

        // Lưu tên avatar
        selectedAvatar = avatarName;


        // Đổi ảnh avatar hiện tại
        imgSelectedAvatar.setImageResource(
                avatarResource
        );


        // Reset tất cả card
        resetAvatarCards();


        // Highlight card được chọn
        selectedCard.setStrokeColor(
                getColor(R.color.green)
        );

        selectedCard.setStrokeWidth(2);
    }


    // =====================================================
    // RESET AVATAR CARD
    // =====================================================

    private void resetAvatarCards() {

        avatarCard1.setStrokeColor(
                getColor(R.color.avatar_border)
        );

        avatarCard2.setStrokeColor(
                getColor(R.color.avatar_border)
        );

        avatarCard3.setStrokeColor(
                getColor(R.color.avatar_border)
        );

        avatarCard4.setStrokeColor(
                getColor(R.color.avatar_border)
        );

        avatarCard5.setStrokeColor(
                getColor(R.color.avatar_border)
        );

        avatarCard6.setStrokeColor(
                getColor(R.color.avatar_border)
        );

        avatarCard7.setStrokeColor(
                getColor(R.color.avatar_border)
        );

        avatarCard8.setStrokeColor(
                getColor(R.color.avatar_border)
        );


        avatarCard1.setStrokeWidth(1);
        avatarCard2.setStrokeWidth(1);
        avatarCard3.setStrokeWidth(1);
        avatarCard4.setStrokeWidth(1);
        avatarCard5.setStrokeWidth(1);
        avatarCard6.setStrokeWidth(1);
        avatarCard7.setStrokeWidth(1);
        avatarCard8.setStrokeWidth(1);
    }


    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    private void updateProfile() {

        String displayName =
                edtDisplayName
                        .getText()
                        .toString()
                        .trim();


        // =========================
        // KIỂM TRA TÊN
        // =========================

        if (displayName.isEmpty()) {

            edtDisplayName.setError(
                    "Vui lòng nhập tên hiển thị"
            );

            edtDisplayName.requestFocus();

            return;
        }


        // =========================
        // TẠO REQUEST
        // =========================

        SetupProfileRequest request =
                new SetupProfileRequest(
                        displayName,
                        selectedAvatar
                );


        // =========================
        // GỌI API
        // =========================

        authApi.updateProfile(
                userId,
                request
        ).enqueue(
                new Callback<MessageResponse>() {

                    @Override
                    public void onResponse(
                            Call<MessageResponse> call,
                            Response<MessageResponse> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    SetupProfileActivity.this,
                                    "Thiết lập hồ sơ thành công!",
                                    Toast.LENGTH_SHORT
                            ).show();


                            // TODO:
                            // Chuyển sang màn hình chính
                            //
                            // Intent intent =
                            //     new Intent(
                            //         ProfileActivity.this,
                            //         MainActivity.class
                            //     );
                            //
                            // startActivity(intent);
                            //
                            // finish();

                        } else {

                            String error = "";

                            try {

                                if (response.errorBody() != null) {

                                    error =
                                            response
                                                    .errorBody()
                                                    .string();
                                }

                            } catch (Exception e) {

                                error = e.getMessage();
                            }


                            Toast.makeText(
                                    SetupProfileActivity.this,
                                    "Thiết lập hồ sơ thất bại\n"
                                            + error,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<MessageResponse> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                SetupProfileActivity.this,
                                "Không thể kết nối server:\n"
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}