package com.example.orderingapp.dto;

import java.util.LinkedList;

public class Product {
    private String name;
    private String price;
    private String description;
    private LinkedList<String> imagePaths;

    public Product(String name, String price, String description, LinkedList<String> imagePaths) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imagePaths = imagePaths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LinkedList<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(LinkedList<String> imagePaths) {
        this.imagePaths = imagePaths;
    }
}
