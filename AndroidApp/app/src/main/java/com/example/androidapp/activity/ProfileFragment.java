package com.example.androidapp.activity;

import android.content.Intent;
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
import com.example.androidapp.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import android.text.InputType;
import android.widget.EditText;
import com.example.androidapp.model.ChangePasswordRequest;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;

import com.example.androidapp.model.MessageResponse;
import com.example.androidapp.model.SetupProfileRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    // =========================
    // VIEW
    // =========================

    private ImageView imgProfileAvatar;

    private TextView txtProfileUsername;
    private TextView txtProfileEmail;
    private TextView txtDisplayNameValue;
    private TextView txtEmailValue;

    private MaterialButton btnLogout;
    private View layoutEditUsername;
    private String currentAvatar;
    private View btnChangeAvatar;
    private View layoutChangePassword;
    // =========================
    // API + TOKEN
    // =========================

    private UserApi userApi;
    private TokenManager tokenManager;


    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_profile,
                container,
                false
        );


        // =========================
        // ÁNH XẠ VIEW
        // =========================

        imgProfileAvatar =
                view.findViewById(R.id.imgProfileAvatar);
        layoutEditUsername =
                view.findViewById(R.id.layoutEditUsername);
        btnChangeAvatar =
                view.findViewById(R.id.btnChangeAvatar);
        txtProfileUsername =
                view.findViewById(R.id.txtProfileUsername);

        layoutChangePassword =
                view.findViewById(R.id.layoutChangePassword);

        txtProfileEmail =
                view.findViewById(R.id.txtProfileEmail);

        txtDisplayNameValue =
                view.findViewById(R.id.txtDisplayNameValue);

        txtEmailValue =
                view.findViewById(R.id.txtEmailValue);

        btnLogout =
                view.findViewById(R.id.btnLogout);


        // =========================
        // API
        // =========================

        userApi =
                ApiClient.getUserApi(requireContext());

        tokenManager =
                new TokenManager(requireContext());


        // =========================
        // =========================
// LOAD USER
// =========================

        loadCurrentUser();


// =========================
// EDIT USERNAME
// =========================

        layoutEditUsername.setOnClickListener(v ->
                showEditUsernameDialog()
        );


// =========================
// CHANGE AVATAR
// =========================

        btnChangeAvatar.setOnClickListener(v ->
                showAvatarDialog()
        );

        layoutChangePassword.setOnClickListener(v ->
                showChangePasswordDialog()
        );
// =========================
// LOGOUT
// =========================

        btnLogout.setOnClickListener(v ->
                showLogoutConfirmDialog()
        );


        return view;
    }

    // =====================================================
    // LOAD CURRENT USER
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

                            UserResponse user =
                                    response.body();
                            // Lưu avatar hiện tại
                            currentAvatar = user.getAvatar();
                            // Username phía trên
                            txtProfileUsername.setText(
                                    user.getUsername()
                            );

                            // Email phía trên
                            txtProfileEmail.setText(
                                    user.getEmail()
                            );

                            // Username trong card
                            txtDisplayNameValue.setText(
                                    user.getUsername()
                            );

                            // Email trong card
                            txtEmailValue.setText(
                                    user.getEmail()
                            );

                            // Avatar
                            setAvatar(
                                    user.getAvatar()
                            );

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Không thể tải thông tin tài khoản"
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
    private void showLogoutConfirmDialog() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?")

                .setNegativeButton(
                        "Hủy",
                        (dialog, which) -> dialog.dismiss()
                )

                .setPositiveButton(
                        "Đăng xuất",
                        (dialog, which) -> logout()
                )

                .show();
    }
    private void showEditUsernameDialog() {

        EditText input = new EditText(requireContext());

        input.setHint("Nhập tên hiển thị");

        // Lấy username hiện tại đưa vào EditText
        input.setText(
                txtDisplayNameValue.getText().toString()
        );

        // Đưa con trỏ về cuối
        input.setSelection(
                input.getText().length()
        );

        input.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );


        // Tạo dialog
        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setTitle("Đổi tên hiển thị")
                        .setView(input)

                        .setNegativeButton(
                                "Hủy",
                                (d, which) -> d.dismiss()
                        )

                        .setPositiveButton(
                                "Lưu",
                                null
                        )

                        .create();


        // Xử lý nút Lưu
        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String newUsername =
                        input.getText()
                                .toString()
                                .trim();


                // Không cho để trống
                if (newUsername.isEmpty()) {

                    input.setError(
                            "Tên hiển thị không được để trống"
                    );

                    return;
                }


                // Gọi API
                updateUsername(
                        newUsername,
                        dialog
                );
            });
        });


        dialog.show();
    }
    private void updateUsername(
            String newUsername,
            AlertDialog dialog) {

        // Giữ nguyên avatar hiện tại
        SetupProfileRequest request =
                new SetupProfileRequest(
                        newUsername,
                        currentAvatar
                );


        userApi.updateProfile(request).enqueue(
                new Callback<MessageResponse>() {

                    @Override
                    public void onResponse(
                            Call<MessageResponse> call,
                            Response<MessageResponse> response) {

                        if (response.isSuccessful()) {

                            // Cập nhật username phía trên
                            txtProfileUsername.setText(
                                    newUsername
                            );

                            // Cập nhật username trong card
                            txtDisplayNameValue.setText(
                                    newUsername
                            );


                            Toast.makeText(
                                    requireContext(),
                                    "Đã cập nhật tên hiển thị",
                                    Toast.LENGTH_SHORT
                            ).show();


                            // Đóng dialog
                            dialog.dismiss();

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Cập nhật thất bại"
                                            + "\nHTTP "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<MessageResponse> call,
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

    private void showAvatarDialog() {

        View dialogView = LayoutInflater
                .from(requireContext())
                .inflate(
                        R.layout.dialog_select_avatar,
                        null
                );

        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("OK", null)
                        .create();


        ImageView carrot =
                dialogView.findViewById(R.id.avatarCarrot);

        ImageView broccoli =
                dialogView.findViewById(R.id.avatarBroccoli);

        ImageView tomato =
                dialogView.findViewById(R.id.avatarTomato);

        ImageView cabbage =
                dialogView.findViewById(R.id.avatarCabbage);

        ImageView corn =
                dialogView.findViewById(R.id.avatarCorn);

        ImageView avocado =
                dialogView.findViewById(R.id.avatarAvocado);

        ImageView orange =
                dialogView.findViewById(R.id.avatarOrange);

        ImageView chili =
                dialogView.findViewById(R.id.avatarChili);


        // Danh sách avatar để tiện xử lý giao diện
        ImageView[] avatarViews = {
                carrot,
                broccoli,
                tomato,
                cabbage,
                corn,
                avocado,
                orange,
                chili
        };


        // Avatar được chọn tạm thời
        final String[] selectedAvatar = {
                currentAvatar
        };


        // =========================
        // CLICK AVATAR
        // =========================

        carrot.setOnClickListener(v -> {

            selectedAvatar[0] = "avatar_carrot";

            selectAvatarView(
                    carrot,
                    avatarViews
            );
        });


        broccoli.setOnClickListener(v -> {

            selectedAvatar[0] = "avatar_broccoli";

            selectAvatarView(
                    broccoli,
                    avatarViews
            );
        });


        tomato.setOnClickListener(v -> {

            selectedAvatar[0] = "avatar_tomato";

            selectAvatarView(
                    tomato,
                    avatarViews
            );
        });


        cabbage.setOnClickListener(v -> {

            selectedAvatar[0] = "avatar_cabbage";

            selectAvatarView(
                    cabbage,
                    avatarViews
            );
        });


        corn.setOnClickListener(v -> {

            selectedAvatar[0] = "avatar_corn";

            selectAvatarView(
                    corn,
                    avatarViews
            );
        });


        avocado.setOnClickListener(v -> {

            selectedAvatar[0] = "avatar_avocado";

            selectAvatarView(
                    avocado,
                    avatarViews
            );
        });


        orange.setOnClickListener(v -> {

            selectedAvatar[0] = "avatar_orange";

            selectAvatarView(
                    orange,
                    avatarViews
            );
        });


        chili.setOnClickListener(v -> {

            selectedAvatar[0] = "avatar_chili";

            selectAvatarView(
                    chili,
                    avatarViews
            );
        });


        // =========================
        // HIỂN THỊ DIALOG
        // =========================

        dialog.setOnShowListener(d -> {

            // Khi mở dialog, highlight avatar hiện tại
            highlightCurrentAvatar(
                    currentAvatar,
                    avatarViews
            );


            // Chỉ khi bấm OK mới cập nhật
            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                if (selectedAvatar[0] == null) {

                    Toast.makeText(
                            requireContext(),
                            "Vui lòng chọn ảnh đại diện",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                updateAvatar(
                        selectedAvatar[0],
                        dialog
                );
            });
        });


        dialog.show();
    }

    private void selectAvatarView(
            ImageView selected,
            ImageView[] avatarViews) {

        // Reset tất cả avatar
        for (ImageView avatar : avatarViews) {

            avatar.setAlpha(0.45f);
            avatar.setScaleX(1.0f);
            avatar.setScaleY(1.0f);
        }


        // Avatar đang được chọn
        selected.setAlpha(1.0f);
        selected.setScaleX(1.15f);
        selected.setScaleY(1.15f);
    }

    private void highlightCurrentAvatar(
            String currentAvatar,
            ImageView[] avatarViews) {

        if (currentAvatar == null) {
            return;
        }

        switch (currentAvatar) {

            case "avatar_carrot":
                selectAvatarView(
                        avatarViews[0],
                        avatarViews
                );
                break;

            case "avatar_broccoli":
                selectAvatarView(
                        avatarViews[1],
                        avatarViews
                );
                break;

            case "avatar_tomato":
                selectAvatarView(
                        avatarViews[2],
                        avatarViews
                );
                break;

            case "avatar_cabbage":
                selectAvatarView(
                        avatarViews[3],
                        avatarViews
                );
                break;

            case "avatar_corn":
                selectAvatarView(
                        avatarViews[4],
                        avatarViews
                );
                break;

            case "avatar_avocado":
                selectAvatarView(
                        avatarViews[5],
                        avatarViews
                );
                break;

            case "avatar_orange":
                selectAvatarView(
                        avatarViews[6],
                        avatarViews
                );
                break;

            case "avatar_chili":
                selectAvatarView(
                        avatarViews[7],
                        avatarViews
                );
                break;
        }
    }

    private void updateAvatar(
            String newAvatar,
            AlertDialog dialog) {

        String currentUsername =
                txtProfileUsername
                        .getText()
                        .toString()
                        .trim();


        SetupProfileRequest request =
                new SetupProfileRequest(
                        currentUsername,
                        newAvatar
                );


        userApi.updateProfile(request).enqueue(
                new Callback<MessageResponse>() {

                    @Override
                    public void onResponse(
                            Call<MessageResponse> call,
                            Response<MessageResponse> response) {

                        if (response.isSuccessful()) {

                            // Lưu avatar mới trong Fragment
                            currentAvatar = newAvatar;

                            // Đổi ảnh ngay trên giao diện
                            setAvatar(newAvatar);

                            Toast.makeText(
                                    requireContext(),
                                    "Đã cập nhật ảnh đại diện",
                                    Toast.LENGTH_SHORT
                            ).show();

                            dialog.dismiss();

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Cập nhật ảnh đại diện thất bại"
                                            + "\nHTTP "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<MessageResponse> call,
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

    private void showChangePasswordDialog() {

        View dialogView = LayoutInflater
                .from(requireContext())
                .inflate(
                        R.layout.dialog_change_password,
                        null
                );

        TextInputEditText edtCurrentPassword =
                dialogView.findViewById(
                        R.id.edtCurrentPassword
                );

        TextInputEditText edtNewPassword =
                dialogView.findViewById(
                        R.id.edtNewPassword
                );

        TextInputEditText edtConfirmPassword =
                dialogView.findViewById(
                        R.id.edtConfirmPassword
                );


        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .setNegativeButton(
                                "Hủy",
                                null
                        )
                        .setPositiveButton(
                                "Đổi mật khẩu",
                                null
                        )
                        .create();


        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String currentPassword =
                        edtCurrentPassword
                                .getText()
                                .toString();

                String newPassword =
                        edtNewPassword
                                .getText()
                                .toString();

                String confirmPassword =
                        edtConfirmPassword
                                .getText()
                                .toString();


                // Kiểm tra rỗng
                if (currentPassword.isEmpty()) {

                    edtCurrentPassword.setError(
                            "Vui lòng nhập mật khẩu hiện tại"
                    );

                    return;
                }


                if (newPassword.isEmpty()) {

                    edtNewPassword.setError(
                            "Vui lòng nhập mật khẩu mới"
                    );

                    return;
                }


                // Kiểm tra độ dài
                if (newPassword.length() < 8) {

                    edtNewPassword.setError(
                            "Mật khẩu phải có ít nhất 8 ký tự"
                    );

                    return;
                }


                // Kiểm tra xác nhận
                if (!newPassword.equals(confirmPassword)) {

                    edtConfirmPassword.setError(
                            "Mật khẩu xác nhận không khớp"
                    );

                    return;
                }


                // Không cho password mới giống password hiện tại
                if (newPassword.equals(currentPassword)) {

                    edtNewPassword.setError(
                            "Mật khẩu mới phải khác mật khẩu hiện tại"
                    );

                    return;
                }


                changePassword(
                        currentPassword,
                        newPassword,
                        dialog
                );
            });
        });


        dialog.show();
    }
    private void changePassword(
            String currentPassword,
            String newPassword,
            AlertDialog dialog) {

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        currentPassword,
                        newPassword
                );


        userApi.changePassword(request).enqueue(
                new Callback<MessageResponse>() {

                    @Override
                    public void onResponse(
                            Call<MessageResponse> call,
                            Response<MessageResponse> response) {

                        if (response.isSuccessful()) {

                            dialog.dismiss();

                            showPasswordChangedDialog();

                        } else {

                            if (response.code() == 400) {

                                Toast.makeText(
                                        requireContext(),
                                        "Mật khẩu hiện tại không đúng hoặc mật khẩu mới không hợp lệ.",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {

                                Toast.makeText(
                                        requireContext(),
                                        "Đổi mật khẩu thất bại"
                                                + "\nHTTP "
                                                + response.code(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<MessageResponse> call,
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

    private void showPasswordChangedDialog() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Đổi mật khẩu thành công")
                .setMessage(
                        "Mật khẩu của bạn đã được thay đổi. "
                                + "Vui lòng đăng nhập lại."
                )
                .setCancelable(false)
                .setPositiveButton(
                        "Đăng nhập lại",
                        (dialog, which) -> {

                            // Xóa JWT hiện tại
                            tokenManager.clearToken();

                            Intent intent = new Intent(
                                    requireContext(),
                                    LoginActivity.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                        }
                )
                .show();
    }
    // =====================================================
    // AVATAR
    // =====================================================

    private void setAvatar(String avatar) {

        if (avatar == null) {

            imgProfileAvatar.setImageResource(
                    R.drawable.avatar_carrot
            );

            return;
        }

        switch (avatar) {

            case "avatar_broccoli":
                imgProfileAvatar.setImageResource(
                        R.drawable.avatar_broccoli
                );
                break;

            case "avatar_tomato":
                imgProfileAvatar.setImageResource(
                        R.drawable.avatar_tomato
                );
                break;

            case "avatar_cabbage":
                imgProfileAvatar.setImageResource(
                        R.drawable.avatar_cabbage
                );
                break;

            case "avatar_corn":
                imgProfileAvatar.setImageResource(
                        R.drawable.avatar_corn
                );
                break;

            case "avatar_avocado":
                imgProfileAvatar.setImageResource(
                        R.drawable.avatar_avocado
                );
                break;

            case "avatar_orange":
                imgProfileAvatar.setImageResource(
                        R.drawable.avatar_orange
                );
                break;

            case "avatar_chili":
                imgProfileAvatar.setImageResource(
                        R.drawable.avatar_chili
                );
                break;

            case "avatar_carrot":
            default:
                imgProfileAvatar.setImageResource(
                        R.drawable.avatar_carrot
                );
                break;
        }
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    private void logout() {

        // Xóa JWT
        tokenManager.clearToken();


        // Về Login
        Intent intent = new Intent(
                requireContext(),
                LoginActivity.class
        );

        // Xóa các Activity cũ khỏi back stack
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
    }

}
