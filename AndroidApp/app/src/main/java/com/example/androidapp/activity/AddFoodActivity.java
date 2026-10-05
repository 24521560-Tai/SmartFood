package com.example.androidapp.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import java.io.File;
public class AddFoodActivity extends AppCompatActivity {

    // =========================
    // VIEW
    // =========================

    private TextInputEditText edtFoodName;
    private TextInputEditText edtQuantity;
    private TextInputEditText edtExpiryDate;
    private TextInputEditText edtNote;

    private AutoCompleteTextView actUnit;

    private MaterialCardView cardCategory;

    private TextView txtSelectedCategory;
    private MaterialCardView cardFoodImage;
    private android.widget.ImageView imgFoodPreview;
    private View layoutAddImage;

    private android.net.Uri selectedImageUri;
    private android.net.Uri cameraImageUri;

    // =========================
    // DATA
    // =========================

    private int selectedCategoryId = -1;

    private final String[] categoryNames = {
            "Rau củ",
            "Trái cây",
            "Thịt",
            "Hải sản",
            "Sữa & sản phẩm từ sữa",
            "Đồ uống",
            "Đồ khô",
            "Gia vị",
            "Bánh kẹo"
    };

    private final String[] units = {
            "Cái",
            "Quả",
            "Gói",
            "Hộp",
            "Chai",
            "Lon",
            "Kg",
            "Gram",
            "Lít",
            "ml"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        initViews();

        setupBackButton();

        setupExpiryDate();

        setupCategory();

        setupUnitDropdown();

        setupImagePicker();
    }


    // =========================
    // ÁNH XẠ VIEW
    // =========================

    private void initViews() {

        edtFoodName = findViewById(R.id.edtFoodName);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtExpiryDate = findViewById(R.id.edtExpiryDate);
        edtNote = findViewById(R.id.edtNote);
        cardFoodImage = findViewById(R.id.cardFoodImage);
        imgFoodPreview = findViewById(R.id.imgFoodPreview);
        layoutAddImage = findViewById(R.id.layoutAddImage);
        actUnit = findViewById(R.id.actUnit);

        cardCategory = findViewById(R.id.cardCategory);

        txtSelectedCategory =
                findViewById(R.id.txtSelectedCategory);
    }


    // =========================
    // QUAY LẠI
    // =========================

    private void setupBackButton() {

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());
    }
    // Chọn ảnh từ thư viện
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            showImagePreview(uri);
                        }
                    }
            );


    // Chụp ảnh
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success && cameraImageUri != null) {
                            selectedImageUri = cameraImageUri;
                            showImagePreview(cameraImageUri);
                        }
                    }
            );
    private void setupImagePicker() {

        cardFoodImage.setOnClickListener(v ->
                showImageOptions()
        );
    }


    private void showImageOptions() {

        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_select_food_image, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        View btnClose =
                dialogView.findViewById(R.id.btnClose);

        View cardCamera =
                dialogView.findViewById(R.id.cardCamera);

        View cardGallery =
                dialogView.findViewById(R.id.cardGallery);

        View cardDeleteImage =
                dialogView.findViewById(R.id.cardDeleteImage);


        // Chỉ hiện Xóa ảnh khi đang có ảnh
        if (selectedImageUri != null) {
            cardDeleteImage.setVisibility(View.VISIBLE);
        } else {
            cardDeleteImage.setVisibility(View.GONE);
        }


        btnClose.setOnClickListener(v ->
                dialog.dismiss()
        );


        cardCamera.setOnClickListener(v -> {

            dialog.dismiss();

            cameraPermissionLauncher.launch(
                    android.Manifest.permission.CAMERA
            );
        });


        cardGallery.setOnClickListener(v -> {

            dialog.dismiss();

            openGallery();
        });


        cardDeleteImage.setOnClickListener(v -> {

            removeSelectedImage();

            dialog.dismiss();
        });



        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

    }
private void removeSelectedImage() {

    selectedImageUri = null;
    cameraImageUri = null;

    imgFoodPreview.setImageDrawable(null);

    imgFoodPreview.setVisibility(View.GONE);

    layoutAddImage.setVisibility(View.VISIBLE);
}
    private void openGallery() {
        galleryLauncher.launch("image/*");
    }
    private void openCamera() {

        try {

            File imageFile = new File(
                    getCacheDir(),
                    "food_" + System.currentTimeMillis() + ".jpg"
            );

            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    imageFile
            );

            cameraLauncher.launch(cameraImageUri);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Không thể mở camera",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {

                        if (granted) {
                            openCamera();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Bạn cần cấp quyền Camera để chụp ảnh",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
    private void showImagePreview(Uri uri) {

        imgFoodPreview.setImageURI(uri);

        imgFoodPreview.setVisibility(View.VISIBLE);

        layoutAddImage.setVisibility(View.GONE);
    }
    // =========================
    // HẠN SỬ DỤNG
    // =========================

    private void setupExpiryDate() {

        edtExpiryDate.setOnClickListener(v ->
                showDatePicker()
        );
    }


    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, selectedYear,
                         selectedMonth,
                         selectedDay) -> {

                            String date = String.format(
                                    Locale.getDefault(),
                                    "%04d-%02d-%02d",
                                    selectedYear,
                                    selectedMonth + 1,
                                    selectedDay
                            );

                            edtExpiryDate.setText(date);
                        },
                        year,
                        month,
                        day
                );


        // Không cho chọn ngày trước hôm nay
        dialog.getDatePicker()
                .setMinDate(System.currentTimeMillis() - 1000);


        dialog.show();
    }


    // =========================
    // CATEGORY
    // =========================

    private void setupCategory() {

        cardCategory.setOnClickListener(v ->
                showCategoryDialog()
        );
    }


    private void showCategoryDialog() {

        int checkedItem =
                selectedCategoryId == -1
                        ? -1
                        : selectedCategoryId - 1;


        new AlertDialog.Builder(this)
                .setTitle("Chọn danh mục")

                .setSingleChoiceItems(
                        categoryNames,
                        checkedItem,
                        (dialog, which) -> {

                            // ID database bắt đầu từ 1
                            selectedCategoryId = which + 1;

                            txtSelectedCategory.setText(
                                    categoryNames[which]
                            );

                            txtSelectedCategory.setTextColor(
                                    getColor(R.color.black)
                            );

                            dialog.dismiss();
                        })

                .setNegativeButton(
                        "Hủy",
                        (dialog, which) ->
                                dialog.dismiss()
                )

                .show();
    }


    // =========================
    // UNIT
    // =========================

    private void setupUnitDropdown() {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        units
                );

        actUnit.setAdapter(adapter);
    }
}