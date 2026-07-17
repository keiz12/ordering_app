package com.example.orderingapp.dto;

import java.util.UUID;

public class CustomerFeedbackPost {
    private final String apiKey;
    private final String feedback;
    private final String uuid;

    public CustomerFeedbackPost(String apiKey, String feedback) {
        this.apiKey = apiKey;
        this.feedback = feedback;
        uuid = UUID.randomUUID().toString();
    }

    public String getFeedback() {
        return feedback;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "CustomerFeedbackPost{" +
                "apiKey='" + apiKey + '\'' +
                ", feedback='" + feedback + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
