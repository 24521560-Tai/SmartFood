
package com.example.androidapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.model.FoodItemResponse;
import com.bumptech.glide.Glide;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class FoodInventoryAdapter
        extends RecyclerView.Adapter<FoodInventoryAdapter.FoodViewHolder> {

    private final List<FoodItemResponse> foodList;
    public interface OnFoodClickListener {
        void onFoodClick(FoodItemResponse food);
    }
    private final OnFoodClickListener listener;
    public FoodInventoryAdapter(
            List<FoodItemResponse> foodList,
            OnFoodClickListener listener
    ) {
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_food, parent, false);

        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull FoodViewHolder holder, int position) {

        FoodItemResponse food = foodList.get(position);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodClick(food);
            }
        });

        holder.txtFoodName.setText(food.getName());
        holder.txtCategory.setText(food.getCategoryName());

        holder.txtQuantity.setText(
                "Số lượng: " + food.getQuantity() + " " + food.getUnit()
        );

        // Tính số ngày còn lại
        try {
            String date = food.getExpiryDate().substring(0, 10);
            LocalDate expiryDate = LocalDate.parse(date);
            LocalDate today = LocalDate.now();

            long days = ChronoUnit.DAYS.between(today, expiryDate);

            if (days < 0) {
                holder.txtExpiry.setText(
                        "Quá hạn " + Math.abs(days) + " ngày"
                );
                holder.txtExpiry.setTextColor(0xFFD32F2F);

            } else if (days == 0) {
                holder.txtExpiry.setText("Hết hạn hôm nay");
                holder.txtExpiry.setTextColor(0xFFD32F2F);

            } else {
                holder.txtExpiry.setText("Còn " + days + " ngày");

                if (days <= 3) {
                    holder.txtExpiry.setTextColor(0xFFFF9800);
                } else {
                    holder.txtExpiry.setTextColor(0xFF2E7D32);
                }
            }

        } catch (Exception e) {
            holder.txtExpiry.setText("Chưa rõ hạn");
            holder.txtExpiry.setTextColor(0xFF78909C);
        }

        // Hiển thị ảnh
        String imageUrl = food.getImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {

            // Đường dẫn ảnh lưu trên Backend
            if (imageUrl.startsWith("/")) {
                imageUrl = "http://10.230.138.34:5222" + imageUrl;
            }

            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.imgFood);

        } else {
            holder.imgFood.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {

        ImageView imgFood;
        TextView txtFoodName, txtCategory, txtQuantity, txtExpiry;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);

            imgFood = itemView.findViewById(R.id.imgFood);
            txtFoodName = itemView.findViewById(R.id.txtFoodName);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtExpiry = itemView.findViewById(R.id.txtExpiry);
        }
    }
}
