package com.example.androidapp.model;

public class BarcodeResponse {

    private boolean found;
    private String source;
    private String barcode;
    private String name;
    private String brand;
    private String imageUrl;
    private Integer categoryId;

    public boolean isFound() {
        return found;
    }

    public String getSource() {
        return source;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getCategoryId() {
        return categoryId;
    }
}