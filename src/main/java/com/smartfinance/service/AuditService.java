package com.smartfinance.service;

import com.smartfinance.dao.AuditLogDAO;
import com.smartfinance.model.AuditLog;

/**
 * Audit Service — Convenience logger for security & user action logging.
 */
public class AuditService {
    private final AuditLogDAO auditLogDAO;

    public AuditService() {
        this.auditLogDAO = new AuditLogDAO();
    }

    public void log(int userId, String action, String description) {
        AuditLog log = new AuditLog(userId, action, description);
        auditLogDAO.insert(log);
    }
}
