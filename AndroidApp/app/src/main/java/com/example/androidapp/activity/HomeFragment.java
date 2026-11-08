package com.example.androidapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.adapter.FoodInventoryAdapter;
import com.example.androidapp.api.ApiClient;
import com.example.androidapp.api.FoodInventoryApi;
import com.example.androidapp.api.UserApi;
import com.example.androidapp.model.FoodItemResponse;
import com.example.androidapp.model.UserResponse;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;
public class HomeFragment extends Fragment {

    // Thông tin người dùng
    private ImageView imgAvatar;
    private TextView txtGreeting;
    private UserApi userApi;

    // Thông tin kho thực phẩm
    private TextView txtTotalFood;
    private TextView txtExpiredMessage;
    private TextView txtExpiredTitle;
    private TextView btnViewExpiredDetail;
    private View layoutExpiredAlert;

    // Cảnh báo sắp hết hạn
    private View layoutExpiringWarning;
    private TextView txtExpiringCount;
    private TextView btnPriorityFood;

    // RecyclerView
    private RecyclerView recyclerExpiringFoods;
    private FoodInventoryAdapter foodAdapter;

    private final List<FoodItemResponse> priorityFoodList =
            new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable android.view.ViewGroup container,
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

        txtTotalFood = view.findViewById(R.id.txtTotalFood);

        txtExpiredMessage =
                view.findViewById(R.id.txtExpiredMessage);

        txtExpiredTitle =
                view.findViewById(R.id.txtExpiredTitle);

        btnViewExpiredDetail =
                view.findViewById(R.id.btnViewExpiredDetail);

        layoutExpiredAlert =
                view.findViewById(R.id.layoutExpiredAlert);

        layoutExpiringWarning =
                view.findViewById(R.id.layoutExpiringWarning);

        txtExpiringCount =
                view.findViewById(R.id.txtExpiringCount);

        btnPriorityFood =
                view.findViewById(R.id.btnPriorityFood);

        recyclerExpiringFoods =
                view.findViewById(R.id.recyclerExpiringFoods);


        // =========================
        // THIẾT LẬP RECYCLERVIEW
// =========================

        recyclerExpiringFoods.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        foodAdapter = new FoodInventoryAdapter(
                priorityFoodList,
                food -> {
                    Intent intent = new Intent(
                            requireContext(),
                            FoodDetailActivity.class
                    );

                    intent.putExtra("food_id", food.getId());

                    startActivity(intent);
                }
        );

        recyclerExpiringFoods.setAdapter(foodAdapter);

        recyclerExpiringFoods.setNestedScrollingEnabled(false);


        // =========================
        // SỰ KIỆN AVATAR
        // =========================

        imgAvatar.setOnClickListener(v -> {

            Intent intent = new Intent(
                    requireContext(),
                    ProfileActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // SỰ KIỆN THÊM THỰC PHẨM
        // =========================

        View btnAddManual =
                view.findViewById(R.id.cardAddManual);

        if (btnAddManual != null) {

            btnAddManual.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        AddFoodActivity.class
                );

                startActivity(intent);
            });
        }


        // =========================
        // SỰ KIỆN QUÉT BARCODE
        // =========================

        View btnScanBarcode =
                view.findViewById(R.id.cardScanBarcode);

        if (btnScanBarcode != null) {

            btnScanBarcode.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        ScanBarcodeActivity.class
                );

                startActivity(intent);
            });
        }


        // =========================
        // SỰ KIỆN XEM THỰC PHẨM ƯU TIÊN
        // =========================

        btnPriorityFood.setOnClickListener(v -> openInventory());


        // =========================
        // API USER
        // =========================

        userApi = ApiClient.getUserApi(requireContext());

        loadCurrentUser();


        // =========================
        // API THỰC PHẨM
        // =========================

        loadFoodItems();


        return view;
    }


    // =====================================================
    // LẤY DANH SÁCH THỰC PHẨM TỪ BACKEND
    // =====================================================

    private void loadFoodItems() {

        FoodInventoryApi foodApi = ApiClient
                .getRetrofit(requireContext())
                .create(FoodInventoryApi.class);

        foodApi.getMyFoodItems().enqueue(
                new Callback<List<FoodItemResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<FoodItemResponse>> call,
                            Response<List<FoodItemResponse>> response) {

                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<FoodItemResponse> allFoods =
                                    response.body();

                            processFoodData(allFoods);

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Không thể tải thực phẩm. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<FoodItemResponse>> call,
                            Throwable t) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // =====================================================
    // PHÂN LOẠI VÀ XỬ LÝ DỮ LIỆU
    // =====================================================

    private void processFoodData(List<FoodItemResponse> allFoods) {

        LocalDate today = LocalDate.now();

        List<FoodItemResponse> validFoods =
                new ArrayList<>();

        List<FoodItemResponse> expiredFoods =
                new ArrayList<>();

        List<FoodItemResponse> expiringFoods =
                new ArrayList<>();

        // Duyệt toàn bộ thực phẩm lấy từ Backend
        for (FoodItemResponse food : allFoods) {

            Long days = getDaysUntilExpiry(food);

            // Không có ngày hết hạn thì không thể phân loại
            if (days == null) {
                continue;
            }

            // Trường hợp 1: Đã hết hạn
            if (days < 0) {

                expiredFoods.add(food);
            }

            // Trường hợp 2: Chưa hết hạn
            else {

                validFoods.add(food);

                // Còn từ 0 đến 3 ngày
                if (days <= 3) {

                    expiringFoods.add(food);
                }
            }
        }


        // =========================
        // 1. TỔNG SỐ THỰC PHẨM
        // =========================

        txtTotalFood.setText(
                "Hiện có "
                        + validFoods.size()
                        + " thực phẩm tươi ngon"
        );


        // =========================
        // 2. CẢNH BÁO THỰC PHẨM HẾT HẠN
        // =========================

        updateExpiredWarning(expiredFoods.size());


        // =========================
        // 3. CẢNH BÁO SẮP HẾT HẠN
        // =========================

        updateExpiringWarning(expiringFoods.size());


        // =========================
        // 4. CHỌN 3 THỰC PHẨM GẦN HẾT HẠN NHẤT
        // =========================

        Collections.sort(
                validFoods,
                Comparator.comparing(
                        food -> getDaysUntilExpiry(food)
                )
        );

        priorityFoodList.clear();

        int displayCount = Math.min(3, validFoods.size());

        for (int i = 0; i < displayCount; i++) {

            priorityFoodList.add(validFoods.get(i));
        }

        foodAdapter.notifyDataSetChanged();
    }


    // =====================================================
    // TÍNH SỐ NGÀY CÒN LẠI
    // =====================================================

    private Long getDaysUntilExpiry(FoodItemResponse food) {

        try {

            String expiry = food.getExpiryDate();

            if (expiry == null || expiry.isEmpty()) {
                return null;
            }

            LocalDate expiryDate = LocalDate.parse(
                    expiry.substring(0, 10)
            );

            return ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    expiryDate
            );

        } catch (Exception e) {

            return null;
        }
    }


    // =====================================================
    // CẬP NHẬT CẢNH BÁO THỰC PHẨM ĐÃ HẾT HẠN
    // =====================================================

    private void updateExpiredWarning(int expiredCount) {

        if (expiredCount == 0) {

            // Không có thực phẩm hết hạn
            txtExpiredMessage.setVisibility(View.VISIBLE);

            layoutExpiredAlert.setVisibility(View.GONE);

        } else {

            // Có thực phẩm hết hạn
            txtExpiredMessage.setVisibility(View.GONE);

            layoutExpiredAlert.setVisibility(View.VISIBLE);

            txtExpiredTitle.setText(
                    "Có "
                            + expiredCount
                            + " loại thực phẩm đã hết hạn sử dụng."
            );

            // Gạch chân riêng chữ Xem chi tiết
            String message = "Ấn Xem chi tiết để xử lý.";

            SpannableString spannable =
                    new SpannableString(message);

            int start = message.indexOf("Xem chi tiết");

            int end = start + "Xem chi tiết".length();

            spannable.setSpan(
                    new android.text.style.UnderlineSpan(),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            btnViewExpiredDetail.setText(spannable);

            btnViewExpiredDetail.setOnClickListener(
                    v -> openInventory()
            );
        }
    }


    // =====================================================
    // CẬP NHẬT CẢNH BÁO THỰC PHẨM SẮP HẾT HẠN
    // =====================================================

    private void updateExpiringWarning(int expiringCount) {

        // Luôn hiển thị ô cảnh báo
        layoutExpiringWarning.setVisibility(View.VISIBLE);

        if (expiringCount == 0) {

            txtExpiringCount.setText(
                    "Không có thực phẩm nào sắp hết hạn"
            );

            btnPriorityFood.setVisibility(View.GONE);

        } else {

            txtExpiringCount.setText(
                    "Có " + expiringCount + " thực phẩm sắp hết hạn!"
            );

            btnPriorityFood.setVisibility(View.VISIBLE);
        }
    }


    // =====================================================
    // CHUYỂN ĐẾN KHO THỰC PHẨM
    // =====================================================

    private void openInventory() {

        if (getActivity() instanceof HomeActivity) {

            HomeActivity activity =
                    (HomeActivity) getActivity();

            activity.findViewById(R.id.bottomNavigation);

            com.google.android.material.bottomnavigation.BottomNavigationView
                    bottomNavigation =
                    activity.findViewById(R.id.bottomNavigation);

            if (bottomNavigation != null) {

                bottomNavigation.setSelectedItemId(
                        R.id.nav_inventory
                );
            }
        }
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

                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            UserResponse user = response.body();

                            txtGreeting.setText(
                                    "Xin chào, "
                                            + user.getUsername()
                                            + "!"
                            );

                            setAvatar(user.getAvatar());

                        }
                    }

                    @Override
                    public void onFailure(
                            Call<UserResponse> call,
                            Throwable t) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Không thể kết nối server",
                                Toast.LENGTH_SHORT
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
                imgAvatar.setImageResource(R.drawable.avatar_broccoli);
                break;

            case "avatar_tomato":
                imgAvatar.setImageResource(R.drawable.avatar_tomato);
                break;

            case "avatar_cabbage":
                imgAvatar.setImageResource(R.drawable.avatar_cabbage);
                break;

            case "avatar_corn":
                imgAvatar.setImageResource(R.drawable.avatar_corn);
                break;

            case "avatar_avocado":
                imgAvatar.setImageResource(R.drawable.avatar_avocado);
                break;

            case "avatar_orange":
                imgAvatar.setImageResource(R.drawable.avatar_orange);
                break;

            case "avatar_chili":
                imgAvatar.setImageResource(R.drawable.avatar_chili);
                break;

            case "avatar_carrot":
            default:
                imgAvatar.setImageResource(R.drawable.avatar_carrot);
                break;
        }
    }
}