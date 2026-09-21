package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.dto.SubscriptionRequest;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.dao.SubscriptionDAO;
import com.smartfinance.model.Subscription;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionApiController {

    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO();

    @GetMapping
    public ResponseEntity<ApiResponse<List<Subscription>>> getSubscriptions(@CurrentUser UserPrincipal principal) {
        List<Subscription> list = subscriptionDAO.findByUserId(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Subscription>> createSubscription(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody SubscriptionRequest request) {

        LocalDate nextDate = LocalDate.now().plusMonths(1);
        if (request.getNextBillingDate() != null && !request.getNextBillingDate().isBlank()) {
            try {
                nextDate = LocalDate.parse(request.getNextBillingDate().trim());
            } catch (Exception ignore) {}
        }

        Subscription sub = new Subscription(
                principal.getUserId(),
                request.getServiceName().trim(),
                request.getAmount(),
                request.getBillingCycle() != null ? request.getBillingCycle() : "MONTHLY",
                nextDate,
                request.getCategory() != null ? request.getCategory() : "Entertainment",
                request.getStatus() != null ? request.getStatus() : "ACTIVE"
        );

        int id = subscriptionDAO.insert(sub);
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add subscription", 500));
        }

        sub.setSubscriptionId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription added successfully", sub));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Subscription>> updateSubscription(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id,
            @Valid @RequestBody SubscriptionRequest request) {

        Subscription existing = subscriptionDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Subscription not found", 404));
        }

        LocalDate nextDate = existing.getNextBillingDate();
        if (request.getNextBillingDate() != null && !request.getNextBillingDate().isBlank()) {
            try {
                nextDate = LocalDate.parse(request.getNextBillingDate().trim());
            } catch (Exception ignore) {}
        }

        existing.setServiceName(request.getServiceName().trim());
        existing.setAmount(request.getAmount());
        if (request.getBillingCycle() != null) existing.setBillingCycle(request.getBillingCycle());
        existing.setNextBillingDate(nextDate);
        if (request.getCategory() != null) existing.setCategory(request.getCategory());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());

        boolean ok = subscriptionDAO.update(existing);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update subscription", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully", existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscription(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id) {

        Subscription existing = subscriptionDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Subscription not found", 404));
        }

        boolean ok = subscriptionDAO.delete(id);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete subscription", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Subscription deleted successfully", null));
    }
}
