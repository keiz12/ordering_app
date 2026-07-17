package com.example.orderingapp.dto;

public class CustomerFeedback {
    private final String feedbackLocalDateTime;
    private final String feedback;
    private final String uuid;

    public CustomerFeedback(String feedbackLocalDateTime, String feedback, String uuid) {
        this.feedbackLocalDateTime = feedbackLocalDateTime;
        this.feedback = feedback;
        this.uuid = uuid;
    }

    public String getFeedbackLocalDateTime() {
        return feedbackLocalDateTime;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "CustomerFeedback{" +
                "feedbackLocalDateTime='" + feedbackLocalDateTime + '\'' +
                ", feedback='" + feedback + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
