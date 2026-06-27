package com.example.orderingapp.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class Order implements Serializable {
    private HashMap<String, Integer> productNameToQty = new HashMap<>();
    private HashMap<String, BigDecimal> productNameToPrice = new HashMap<>();
    private LocalDateTime createdAt;
    private int tableNumber;
    private String uuid;
    private boolean isPaid;
    private String apiKey;

    public HashMap<String, Integer> getProductNameToQty() {
        return productNameToQty;
    }

    public void setProductNameToQty(HashMap<String, Integer> productNameToQty) {
        this.productNameToQty = productNameToQty;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public HashMap<String, BigDecimal> getProductNameToPrice() {
        return productNameToPrice;
    }

    public void setProductNameToPrice(HashMap<String, BigDecimal> productNameToPrice) {
        this.productNameToPrice = productNameToPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

