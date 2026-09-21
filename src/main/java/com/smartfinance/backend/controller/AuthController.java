package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.dto.AuthRequest;
import com.smartfinance.backend.dto.AuthResponse;
import com.smartfinance.backend.dto.RegisterRequest;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.JwtUtil;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.dao.UserDAO;
import com.smartfinance.model.User;
import com.smartfinance.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService = new AuthService();
    private final UserDAO userDAO = new UserDAO();
    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        User user = authService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username/email or password.", 401));
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getName(), user.getRole());
        AuthResponse response = new AuthResponse(
                token,
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCurrency()
        );

        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (userDAO.emailExists(cleanEmail)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("An account with this email address already exists.", 400));
        }

        User user = authService.register(
                request.getName(),
                cleanEmail,
                request.getPassword(),
                request.getRole(),
                request.getAge()
        );

        if (user == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed. Please verify input data.", 500));
        }

        // Update optional profile details if supplied
        if (request.getPhone() != null || request.getCurrency() != null || request.getIncomeRange() != null) {
            if (request.getPhone() != null) user.setPhone(request.getPhone());
            if (request.getCurrency() != null) user.setCurrency(request.getCurrency());
            if (request.getIncomeRange() != null) user.setIncomeRange(request.getIncomeRange());
            userDAO.update(user);
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getName(), user.getRole());
        AuthResponse response = new AuthResponse(
                token,
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCurrency()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(@CurrentUser UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated", 401));
        }

        User user = userDAO.findById(principal.getUserId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found", 404));
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        profile.put("age", user.getAge());
        profile.put("phone", user.getPhone());
        profile.put("currency", user.getCurrency());
        profile.put("incomeRange", user.getIncomeRange());
        profile.put("status", user.getStatus());
        profile.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
