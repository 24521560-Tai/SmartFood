package com.example.androidapp.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.androidapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Khi mới mở HomeActivity -> mặc định hiển thị HomeFragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Xử lý khi bấm Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment selectedFragment;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {

                selectedFragment = new HomeFragment();

            } else if (itemId == R.id.nav_inventory) {

                selectedFragment = new FoodInventoryFragment();

            } else if (itemId == R.id.nav_recipe) {

                selectedFragment = new RecipeFragment();

            } else if (itemId == R.id.nav_profile) {

                selectedFragment = new ProfileFragment();

            } else {

                return false;
            }

            loadFragment(selectedFragment);

            return true;
        });
    }


    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

    }
}