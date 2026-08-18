package com.smartfinance.model;

import java.time.LocalDate;

/**
 * Notification model for alerts and reminders.
 * Demonstrates: Encapsulation, OOP.
 */
public class Notification {
    private int notificationId;
    private int userId;
    private String message;
    private LocalDate date;
    private boolean read;

    public Notification() {}

    public Notification(int userId, String message, LocalDate date, boolean read) {
        this.userId = userId;
        this.message = message;
        this.date = date;
        this.read = read;
    }

    public Notification(int notificationId, int userId, String message,
                        LocalDate date, boolean read) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.date = date;
        this.read = read;
    }

    // Getters and Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    @Override
    public String toString() {
        return "Notification{id=" + notificationId + ", message='" + message +
               "', read=" + read + ", date=" + date + "}";
    }
}
