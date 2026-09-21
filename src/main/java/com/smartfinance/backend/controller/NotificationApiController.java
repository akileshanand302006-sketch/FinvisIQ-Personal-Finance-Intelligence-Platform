package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.dao.NotificationDAO;
import com.smartfinance.model.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(@CurrentUser UserPrincipal principal) {
        List<Notification> list = notificationDAO.findByUserId(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Notification>> createNotification(
            @CurrentUser UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "System Alert");
        Notification n = new Notification(principal.getUserId(), message, LocalDate.now(), false);
        int id = notificationDAO.insert(n);
        if (id > 0) {
            n.setNotificationId(id);
            return ResponseEntity.ok(ApiResponse.success("Notification created", n));
        }
        return ResponseEntity.status(500).body(ApiResponse.error("Failed to create notification", 500));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id) {
        boolean ok = notificationDAO.markAsRead(id);
        return ok ? ResponseEntity.ok(ApiResponse.success("Marked as read", null))
                : ResponseEntity.status(500).body(ApiResponse.error("Failed to update notification", 500));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id) {
        boolean ok = notificationDAO.delete(id);
        return ok ? ResponseEntity.ok(ApiResponse.success("Notification deleted", null))
                : ResponseEntity.status(500).body(ApiResponse.error("Failed to delete notification", 500));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications(@CurrentUser UserPrincipal principal) {
        boolean ok = notificationDAO.deleteAll(principal.getUserId());
        return ok ? ResponseEntity.ok(ApiResponse.success("All notifications cleared", null))
                : ResponseEntity.status(500).body(ApiResponse.error("Failed to clear notifications", 500));
    }
}
