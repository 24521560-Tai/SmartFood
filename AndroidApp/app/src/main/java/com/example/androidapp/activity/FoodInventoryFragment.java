package com.example.androidapp.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.adapter.FoodInventoryAdapter;
import com.example.androidapp.api.ApiClient;
import com.example.androidapp.api.FoodInventoryApi;
import com.example.androidapp.model.FoodItemResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodInventoryFragment extends Fragment {

    private RecyclerView recyclerFoodItems;
    private FoodInventoryAdapter adapter;

    private final List<FoodItemResponse> foodList =
            new ArrayList<>();

    public FoodInventoryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_food_inventory,
                container,
                false
        );

        // 1. Ánh xạ RecyclerView
        recyclerFoodItems =
                view.findViewById(R.id.recyclerFoodItems);

        // 2. Thiết lập danh sách dạng dọc
        recyclerFoodItems.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        // 3. Tạo Adapter
        adapter = new FoodInventoryAdapter(foodList);

        recyclerFoodItems.setAdapter(adapter);

        // 4. Lấy dữ liệu từ Backend
        loadFoodItems();

        return view;
    }

    private void loadFoodItems() {

        FoodInventoryApi api = ApiClient
                .getRetrofit(requireContext())
                .create(FoodInventoryApi.class);

        api.getMyFoodItems().enqueue(
                new Callback<List<FoodItemResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<FoodItemResponse>> call,
                            Response<List<FoodItemResponse>> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful()
                                && response.body() != null) {

                            foodList.clear();
                            foodList.addAll(response.body());

                            adapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Không thể tải danh sách thực phẩm. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<FoodItemResponse>> call,
                            Throwable t) {

                        if (!isAdded()) return;

                        Toast.makeText(
                                requireContext(),
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}