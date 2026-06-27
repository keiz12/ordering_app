package com.example.orderingapp.dto;

import com.google.gson.annotations.SerializedName;

public class OrderProcessIndicator {
    private String uuid;
    @SerializedName(value = "isProcessed", alternate = {"processed", "is_processed"})
    private boolean isProcessed;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }
}
