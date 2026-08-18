package com.smartfinance.model;

import java.time.LocalDateTime;

/**
 * Financial Insight Model — Prioritized, intelligent rule-based insight item.
 */
public class FinancialInsight {
    public enum Priority { INFO, SUCCESS, WARNING, CRITICAL }

    private Priority priority;
    private String category;
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private boolean read = false;

    public FinancialInsight(Priority priority, String category, String title, String message) {
        this.priority = priority;
        this.category = category;
        this.title = title;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }

    public Priority getPriority() { return priority; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getPriorityStyleClass() {
        return switch (priority) {
            case SUCCESS -> "insight-success";
            case WARNING -> "insight-warning";
            case CRITICAL -> "insight-critical";
            default -> "insight-info";
        };
    }
}
