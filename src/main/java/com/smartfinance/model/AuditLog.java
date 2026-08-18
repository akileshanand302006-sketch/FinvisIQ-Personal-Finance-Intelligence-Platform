package com.smartfinance.model;

import java.time.LocalDateTime;

/**
 * Audit Log Model — Action auditing for security & tracking user actions.
 */
public class AuditLog {
    private int logId;
    private int userId;
    private String action;
    private String description;
    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog() {}

    public AuditLog(int userId, String action, String description) {
        this.userId = userId;
        this.action = action;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(int logId, int userId, String action, String description, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
