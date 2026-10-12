package com.example.androidapp.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.androidapp.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import com.example.androidapp.api.ApiClient;
import com.example.androidapp.api.BarcodeApi;
import com.example.androidapp.model.BarcodeResponse;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
@androidx.camera.core.ExperimentalGetImage
public class ScanBarcodeActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView txtScanStatus;

    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private TextView txtBarcode;
    private TextView txtFoodName;
    private TextView txtBrand;

    private Button btnContinue;
    private Button btnScanAgain;

    private BarcodeResponse currentFood;
    private final AtomicBoolean isScanned =
            new AtomicBoolean(false);

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            startCamera();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Cần cấp quyền Camera để quét Barcode",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan_barcode);

        previewView = findViewById(R.id.previewView);
        txtScanStatus = findViewById(R.id.txtScanStatus);

        txtBarcode = findViewById(R.id.txtBarcode);
        txtFoodName = findViewById(R.id.txtFoodName);
        txtBrand = findViewById(R.id.txtBrand);

        btnContinue = findViewById(R.id.btnContinue);
        btnScanAgain = findViewById(R.id.btnScanAgain);

        btnScanAgain.setOnClickListener(v -> resetScanner());

        btnContinue.setOnClickListener(v -> {

            if (currentFood == null) {
                Toast.makeText(this,
                        "Chưa có dữ liệu sản phẩm",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String barcode = currentFood.getBarcode();

            if (barcode == null || barcode.trim().isEmpty()) {
                Toast.makeText(this,
                        "Barcode không hợp lệ",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(
                    ScanBarcodeActivity.this,
                    AddFoodActivity.class
            );

            intent.putExtra("mode", "BARCODE");
            intent.putExtra("barcode", barcode);

            // Tên sản phẩm
            if (currentFood.getName() != null) {
                intent.putExtra("name", currentFood.getName());
            }

            // Thương hiệu
            if (currentFood.getBrand() != null) {
                intent.putExtra("brand", currentFood.getBrand());
            }

            // Ảnh sản phẩm
            if (currentFood.getImageUrl() != null) {
                intent.putExtra("imageUrl", currentFood.getImageUrl());
            }

            // Danh mục
            if (currentFood.getCategoryId() != null) {
                intent.putExtra(
                        "categoryId",
                        currentFood.getCategoryId()
                );
            }

            startActivity(intent);
        });

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());

        cameraExecutor = Executors.newSingleThreadExecutor();

        barcodeScanner = BarcodeScanning.getClient();

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            startCamera();

        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();

                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider()
                );

                ImageAnalysis analysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                )
                                .build();

                analysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        txtScanStatus.setText(
                                "Không thể mở Camera: " + e.getMessage()
                        )
                );
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(@NonNull ImageProxy imageProxy) {

        if (isScanned.get()) {
            imageProxy.close();
            return;
        }

        android.media.Image mediaImage = imageProxy.getImage();

        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.getImageInfo().getRotationDegrees()
        );

        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {

                    for (Barcode barcode : barcodes) {

                        String value = barcode.getRawValue();

                        if (value != null
                                && !value.isEmpty()
                                && isScanned.compareAndSet(false, true)) {

                            runOnUiThread(() -> {
                                txtScanStatus.setText("Đang tra cứu sản phẩm...");
                                txtBarcode.setText("Barcode: " + value);

                                lookupBarcode(value);
                            });

                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Tiếp tục quét ở frame kế tiếp
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }
    private void lookupBarcode(String barcode) {

        BarcodeApi api = ApiClient.getBarcodeApi(this);

        api.getFoodByBarcode(barcode)
                .enqueue(new Callback<BarcodeResponse>() {

                    @Override
                    public void onResponse(
                            Call<BarcodeResponse> call,
                            Response<BarcodeResponse> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            txtScanStatus.setText(
                                    "Tra cứu thất bại. Vui lòng thử lại."
                            );

                            btnScanAgain.setVisibility(View.VISIBLE);
                            return;
                        }

                        currentFood = response.body();

                        txtBarcode.setText(
                                "Barcode: " + currentFood.getBarcode()
                        );

                        if (currentFood.isFound()) {

                            txtScanStatus.setText(
                                    "Đã tìm thấy sản phẩm"
                            );

                            txtFoodName.setText(
                                    "Tên sản phẩm: " + currentFood.getName()
                            );

                            txtBrand.setText(
                                    "Thương hiệu: " +
                                            (currentFood.getBrand() == null
                                                    ? "Không có"
                                                    : currentFood.getBrand())
                            );

                        } else {

                            txtScanStatus.setText(
                                    "Không tìm thấy sản phẩm. Vui lòng nhập thông tin."
                            );

                            txtFoodName.setText(
                                    "Tên sản phẩm: Chưa có"
                            );

                            txtBrand.setText(
                                    "Thương hiệu: Chưa có"
                            );
                        }

                        btnContinue.setVisibility(View.VISIBLE);
                        btnScanAgain.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(
                            Call<BarcodeResponse> call,
                            Throwable t) {

                        txtScanStatus.setText(
                                "Lỗi kết nối: " + t.getMessage()
                        );

                        btnScanAgain.setVisibility(View.VISIBLE);
                    }
                });
    }
    private void resetScanner() {

        currentFood = null;

        isScanned.set(false);

        txtScanStatus.setText("Đang chờ quét Barcode...");
        txtBarcode.setText("Barcode: --");
        txtFoodName.setText("Tên sản phẩm: --");
        txtBrand.setText("Thương hiệu: --");

        btnContinue.setVisibility(View.GONE);
        btnScanAgain.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (barcodeScanner != null) {
            barcodeScanner.close();
        }

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}