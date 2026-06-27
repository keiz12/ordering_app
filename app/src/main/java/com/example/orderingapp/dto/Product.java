package com.example.orderingapp.dto;

import java.util.*;

public class Product {
    private String name;
    private double price;
    private String description;
    private HashMap<String, String> imagePathToDeletePath = new HashMap<>();
    private List<String> imageURLPath = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, String> getImagePathToDeletePath() {
        return imagePathToDeletePath;
    }

    public List<String> getImageURLPath() {
        return imageURLPath;
    }

    public void setImagePathToDeletePath(HashMap<String, String> imagePathToDeletePath) {
        this.imagePathToDeletePath = imagePathToDeletePath;
    }

    public void setImageURLPath(List<String> imageURLPath) {
        this.imageURLPath = imageURLPath;
    }
}
