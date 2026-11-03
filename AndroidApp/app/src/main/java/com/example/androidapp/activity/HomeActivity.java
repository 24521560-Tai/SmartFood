package com.example.androidapp.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.androidapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAddFood = findViewById(R.id.fabAddFood);

        // Xử lý Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment selectedFragment;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();

            } else if (itemId == R.id.nav_inventory) {
                selectedFragment = new FoodInventoryFragment();

            } else if (itemId == R.id.nav_recipe) {
                selectedFragment = new RecipeFragment();

            } else if (itemId == R.id.nav_ai_suggestion) {
                selectedFragment = new AiFragment();

            } else {
                return false;
            }

            loadFragment(selectedFragment);
            return true;
        });
        // Mặc định hiển thị Trang chủ
        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        // Nhấn dấu + để chọn cách thêm thực phẩm
        fabAddFood.setOnClickListener(v -> showAddFoodDialog());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void showAddFoodDialog() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);

        dialog.setContentView(R.layout.bottom_sheet_add_food);

        dialog.findViewById(R.id.optionBarcode)
                .setOnClickListener(v -> {
                    dialog.dismiss();

                    Intent intent = new Intent(
                            this, ScanBarcodeActivity.class
                    );
                    startActivity(intent);
                });

        dialog.findViewById(R.id.optionManual)
                .setOnClickListener(v -> {
                    dialog.dismiss();

                    Intent intent = new Intent(
                            this, AddFoodActivity.class
                    );
                    startActivity(intent);
                });

        dialog.show();
    }
}