
package com.example.androidapp.model;

public class FoodItemResponse {

    private int id;
    private String name;
    private Integer categoryId;
    private String categoryName;
    private String imageUrl;
    private double quantity;
    private String unit;
    private String expiryDate;
    private String note;
    private String addedAt;
    private String barcode;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getNote() {
        return note;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public String getBarcode() {
        return barcode;
    }
}
