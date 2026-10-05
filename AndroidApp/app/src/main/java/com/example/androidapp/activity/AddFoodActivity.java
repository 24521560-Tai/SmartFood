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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.OpenableColumns;

import com.example.androidapp.api.ApiClient;
import com.example.androidapp.api.FoodItemApi;
import com.example.androidapp.model.AddFoodResponse;
import com.google.android.material.button.MaterialButton;

import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class AddFoodActivity extends AppCompatActivity {

    // =========================
    // VIEW
    // =========================

    private TextInputEditText edtFoodName;
    private TextInputEditText edtQuantity;
    private TextInputEditText edtExpiryDate;
    private TextInputEditText edtNote;
    private MaterialButton btnAddFood;
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
        setupAddFoodButton();
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
        btnAddFood = findViewById(R.id.btnAddFood);
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
    private void setupAddFoodButton() {

        btnAddFood.setOnClickListener(v -> {

            if (validateInput()) {
                uploadFood();
            }
        });
    }
    private boolean validateInput() {

        String name = edtFoodName.getText() == null
                ? ""
                : edtFoodName.getText().toString().trim();

        String quantityText = edtQuantity.getText() == null
                ? ""
                : edtQuantity.getText().toString().trim();

        String unit = actUnit.getText()
                .toString()
                .trim();

        String expiryDate = edtExpiryDate.getText() == null
                ? ""
                : edtExpiryDate.getText().toString().trim();


        // Tên
        if (name.isEmpty()) {

            edtFoodName.setError(
                    "Vui lòng nhập tên thực phẩm"
            );

            edtFoodName.requestFocus();

            return false;
        }


        // Category
        if (selectedCategoryId == -1) {

            Toast.makeText(
                    this,
                    "Vui lòng chọn danh mục",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }


        // Quantity
        if (quantityText.isEmpty()) {

            edtQuantity.setError(
                    "Vui lòng nhập số lượng"
            );

            edtQuantity.requestFocus();

            return false;
        }


        try {

            double quantity =
                    Double.parseDouble(quantityText);

            if (quantity <= 0) {

                edtQuantity.setError(
                        "Số lượng phải lớn hơn 0"
                );

                edtQuantity.requestFocus();

                return false;
            }

        } catch (NumberFormatException e) {

            edtQuantity.setError(
                    "Số lượng không hợp lệ"
            );

            edtQuantity.requestFocus();

            return false;
        }


        // Unit
        if (unit.isEmpty()) {

            Toast.makeText(
                    this,
                    "Vui lòng chọn đơn vị",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }


        // ExpiryDate
        if (expiryDate.isEmpty()) {

            Toast.makeText(
                    this,
                    "Vui lòng chọn hạn sử dụng",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }


        return true;
    }
    private void uploadFood() {

        String name =
                edtFoodName.getText().toString().trim();

        String quantity =
                edtQuantity.getText().toString().trim();

        String unit =
                actUnit.getText().toString().trim();

        String expiryDate =
                edtExpiryDate.getText().toString().trim();

        String note = edtNote.getText() == null
                ? ""
                : edtNote.getText().toString().trim();


        RequestBody nameBody =
                createTextBody(name);

        RequestBody categoryBody =
                createTextBody(
                        String.valueOf(selectedCategoryId)
                );

        RequestBody quantityBody =
                createTextBody(quantity);

        RequestBody unitBody =
                createTextBody(unit);

        RequestBody expiryBody =
                createTextBody(expiryDate);

        RequestBody noteBody =
                createTextBody(note);


        MultipartBody.Part imagePart = null;

        try {

            if (selectedImageUri != null) {

                imagePart =
                        createImagePart(selectedImageUri);
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Không thể đọc ảnh đã chọn",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        setLoading(true);


        FoodItemApi api =
                ApiClient.getFoodItemApi(this);


        api.addManualFood(
                nameBody,
                categoryBody,
                quantityBody,
                unitBody,
                expiryBody,
                noteBody,
                imagePart
        ).enqueue(new Callback<AddFoodResponse>() {

            @Override
            public void onResponse(
                    Call<AddFoodResponse> call,
                    Response<AddFoodResponse> response) {

                setLoading(false);

                if (response.isSuccessful()
                        && response.body() != null) {

                    Toast.makeText(
                            AddFoodActivity.this,
                            response.body().getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                } else {

                    Toast.makeText(
                            AddFoodActivity.this,
                            "Không thể thêm thực phẩm",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }


            @Override
            public void onFailure(
                    Call<AddFoodResponse> call,
                    Throwable t) {

                setLoading(false);

                Toast.makeText(
                        AddFoodActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
    private RequestBody createTextBody(String value) {

        return RequestBody.create(
                value,
                MediaType.parse("text/plain")
        );
    }
    private MultipartBody.Part createImagePart(Uri uri)
            throws Exception {

        ContentResolver resolver =
                getContentResolver();

        String fileName =
                getFileName(uri);

        if (fileName == null
                || fileName.trim().isEmpty()) {

            fileName =
                    "food_" +
                            System.currentTimeMillis() +
                            ".jpg";
        }


        File tempFile =
                new File(
                        getCacheDir(),
                        fileName
                );


        try (
                InputStream inputStream =
                        resolver.openInputStream(uri);

                FileOutputStream outputStream =
                        new FileOutputStream(tempFile)
        ) {

            if (inputStream == null) {
                throw new Exception(
                        "Không thể mở ảnh"
                );
            }


            byte[] buffer =
                    new byte[8192];

            int length;

            while ((length =
                    inputStream.read(buffer)) > 0) {

                outputStream.write(
                        buffer,
                        0,
                        length
                );
            }
        }


        String mimeType =
                resolver.getType(uri);

        if (mimeType == null) {
            mimeType = "image/jpeg";
        }


        RequestBody imageBody =
                RequestBody.create(
                        tempFile,
                        MediaType.parse(mimeType)
                );


        return MultipartBody.Part.createFormData(
                "Image",
                tempFile.getName(),
                imageBody
        );
    }
    private String getFileName(Uri uri) {

        String result = null;


        if ("content".equals(uri.getScheme())) {

            try (
                    Cursor cursor =
                            getContentResolver().query(
                                    uri,
                                    null,
                                    null,
                                    null,
                                    null
                            )
            ) {

                if (cursor != null
                        && cursor.moveToFirst()) {

                    int index =
                            cursor.getColumnIndex(
                                    OpenableColumns.DISPLAY_NAME
                            );

                    if (index >= 0) {
                        result =
                                cursor.getString(index);
                    }
                }
            }
        }


        if (result == null) {

            String path =
                    uri.getPath();

            if (path != null) {

                int cut =
                        path.lastIndexOf('/');

                result = cut >= 0
                        ? path.substring(cut + 1)
                        : path;
            }
        }


        return result;
    }
    private void setLoading(boolean loading) {

        btnAddFood.setEnabled(!loading);

        if (loading) {

            btnAddFood.setText(
                    "Đang thêm..."
            );

        } else {

            btnAddFood.setText(
                    "Thêm thực phẩm"
            );
        }
    }
    private void showImageOptions() {

        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_select_food_image, null);

        BottomSheetDialog dialog =
                new BottomSheetDialog(this);

        dialog.setContentView(dialogView);


        View btnClose =
                dialogView.findViewById(R.id.btnClose);

        View cardCamera =
                dialogView.findViewById(R.id.cardCamera);

        View cardGallery =
                dialogView.findViewById(R.id.cardGallery);

        View cardDeleteImage =
                dialogView.findViewById(R.id.cardDeleteImage);


        // Chỉ hiện nút Xóa khi đã chọn ảnh
        if (selectedImageUri != null) {
            cardDeleteImage.setVisibility(View.VISIBLE);
        } else {
            cardDeleteImage.setVisibility(View.GONE);
        }


        // Đóng Bottom Sheet
        btnClose.setOnClickListener(v ->
                dialog.dismiss()
        );


        // Camera
        cardCamera.setOnClickListener(v -> {

            dialog.dismiss();

            cameraPermissionLauncher.launch(
                    android.Manifest.permission.CAMERA
            );
        });


        // Gallery
        cardGallery.setOnClickListener(v -> {

            dialog.dismiss();

            openGallery();
        });


        // Xóa ảnh
        cardDeleteImage.setOnClickListener(v -> {

            removeSelectedImage();

            dialog.dismiss();
        });


        dialog.show();
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