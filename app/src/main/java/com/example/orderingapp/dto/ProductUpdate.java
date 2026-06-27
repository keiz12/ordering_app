package com.example.orderingapp.dto;

public class ProductUpdate {
    private String oldProductName;
    private Product newProduct;

    public String getOldProductName() {
        return oldProductName;
    }

    public void setOldProductName(String oldProductName) {
        this.oldProductName = oldProductName;
    }

    public Product getNewProduct() {
        return newProduct;
    }

    public void setNewProduct(Product newProduct) {
        this.newProduct = newProduct;
    }

    @Override
    public String toString() {
        return "ProductUpdate{" +
                "oldProductName='" + oldProductName + '\'' +
                ", newProduct=" + newProduct +
                '}';
    }
}
