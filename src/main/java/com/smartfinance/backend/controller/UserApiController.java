package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.dao.UserDAO;
import com.smartfinance.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserDAO userDAO = new UserDAO();

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @CurrentUser UserPrincipal principal,
            @RequestBody Map<String, Object> body) {

        User existing = userDAO.findByEmail(principal.getEmail());
        if (existing == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("User not found", 404));
        }

        if (body.containsKey("name") && body.get("name") != null) {
            existing.setName(body.get("name").toString());
        }
        if (body.containsKey("phone") && body.get("phone") != null) {
            existing.setPhone(body.get("phone").toString());
        }
        if (body.containsKey("currency") && body.get("currency") != null) {
            existing.setCurrency(body.get("currency").toString());
        }
        if (body.containsKey("incomeRange") && body.get("incomeRange") != null) {
            existing.setIncomeRange(body.get("incomeRange").toString());
        }

        boolean updated = userDAO.update(existing);
        if (updated) {
            existing.setPassword(null); // Never return password in response
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", existing));
        } else {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to update profile", 500));
        }
    }
}
