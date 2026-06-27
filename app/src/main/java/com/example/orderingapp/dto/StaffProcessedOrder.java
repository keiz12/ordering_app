package com.example.orderingapp.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StaffProcessedOrder implements Serializable {

    private Order order;
    private String processedBy;
    private LocalDateTime processedAt;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
