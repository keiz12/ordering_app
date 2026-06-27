package com.example.orderingapp.dto.image;

public class IMGBBResponse {

    private IMGData data;
    private String status;
    private String success;

    public IMGData getData() {
        return data;
    }

    public void setData(IMGData data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
}
