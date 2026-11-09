package com.example.androidapp.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.androidapp.api.ApiClient;
import com.example.androidapp.api.FoodInventoryApi;
import com.example.androidapp.model.FoodItemResponse;

import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.androidapp.R;

public class FoodDetailActivity extends AppCompatActivity {

    private ImageView imgFood;
    private TextView txtFoodName, txtCategory;
    private TextView txtQuantity, txtExpiryDate;
    private TextView txtBarcode, txtAddedAt, txtNote;

    private ImageButton btnBack;
    private Button btnEdit, btnDelete;

    private static final String IMAGE_BASE_URL =
            "http://20.39.193.144:5000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        // 1. Ánh xạ các thành phần giao diện
        initViews();

        // 2. Xử lý nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // 3. Nhận ID thực phẩm từ Intent
        int foodId = getIntent().getIntExtra("food_id", -1);

        if (foodId == -1) {
            Toast.makeText(this,
                    "Không tìm thấy ID thực phẩm",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 4. Gọi API lấy thông tin chi tiết
        loadFoodDetail(foodId);
    }

    private void initViews() {

        imgFood = findViewById(R.id.imgFood);

        txtFoodName = findViewById(R.id.txtFoodName);
        txtCategory = findViewById(R.id.txtCategory);

        txtQuantity = findViewById(R.id.txtQuantity);
        txtExpiryDate = findViewById(R.id.txtExpiryDate);

        txtBarcode = findViewById(R.id.txtBarcode);
        txtAddedAt = findViewById(R.id.txtAddedAt);
        txtNote = findViewById(R.id.txtNote);

        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void loadFoodDetail(int foodId) {

        FoodInventoryApi api = ApiClient
                .getRetrofit(this)
                .create(FoodInventoryApi.class);

        api.getFoodItemById(foodId).enqueue(new Callback<FoodItemResponse>() {

            @Override
            public void onResponse(
                    Call<FoodItemResponse> call,
                    Response<FoodItemResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    FoodItemResponse food = response.body();

                    displayFoodDetail(food);

                } else if (response.code() == 401) {

                    Toast.makeText(FoodDetailActivity.this,
                            "Phiên đăng nhập hết hạn",
                            Toast.LENGTH_SHORT).show();

                } else if (response.code() == 404) {

                    Toast.makeText(FoodDetailActivity.this,
                            "Không tìm thấy thực phẩm",
                            Toast.LENGTH_SHORT).show();

                    finish();

                } else {

                    Toast.makeText(FoodDetailActivity.this,
                            "Không thể tải thông tin thực phẩm",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(
                    Call<FoodItemResponse> call,
                    Throwable t) {

                Toast.makeText(FoodDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayFoodDetail(FoodItemResponse food) {

        // 1. Tên thực phẩm
        txtFoodName.setText(food.getName());

        // 2. Danh mục
        String category = food.getCategoryName();

        if (category != null && !category.isEmpty()) {
            txtCategory.setText(category);
        } else {
            txtCategory.setText("Chưa phân loại");
        }

        // 3. Số lượng
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.##");

        String quantity = decimalFormat.format(food.getQuantity())
                + " " + food.getUnit();

        txtQuantity.setText(quantity);

        // 4. Hạn sử dụng
        txtExpiryDate.setText(formatDate(food.getExpiryDate()));

        // 5. Mã vạch
        String barcode = food.getBarcode();

        if (barcode != null && !barcode.isEmpty()) {
            txtBarcode.setText(barcode);
        } else {
            txtBarcode.setText("Không có");
        }

        // 6. Ngày thêm vào kho
        txtAddedAt.setText(formatDate(food.getAddedAt()));

        // 7. Ghi chú
        String note = food.getNote();

        if (note != null && !note.trim().isEmpty()) {
            txtNote.setText(note);
        } else {
            txtNote.setText("Chưa có ghi chú");
        }

        // 8. Ảnh thực phẩm
        String imageUrl = food.getImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {

            if (imageUrl.startsWith("/")) {
                imageUrl = IMAGE_BASE_URL + imageUrl;
            }

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(imgFood);

        } else {
            imgFood.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    // Chuyển ngày từ yyyy-MM-dd sang dd/MM/yyyy
    private String formatDate(String date) {

        if (date == null || date.isEmpty()) {
            return "Không có";
        }

        try {
            String datePart = date.substring(0, 10);
            String[] parts = datePart.split("-");

            return parts[2] + "/" + parts[1] + "/" + parts[0];

        } catch (Exception e) {
            return date;
        }
    }
}
