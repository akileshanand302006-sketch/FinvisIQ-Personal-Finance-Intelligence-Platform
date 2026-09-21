package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.dto.BudgetRequest;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.dao.BudgetDAO;
import com.smartfinance.model.Budget;
import com.smartfinance.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/budgets")
public class BudgetApiController {

    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final BudgetService budgetService = new BudgetService();

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBudgets(@CurrentUser UserPrincipal principal) {
        int userId = principal.getUserId();
        List<Budget> budgets = budgetDAO.findByUserId(userId);

        List<Map<String, Object>> response = budgets.stream().map(b -> {
            Map<String, Object> item = new HashMap<>();
            double spent = budgetService.getCategorySpent(userId, b.getCategory());
            double pct = b.getBudgetAmount() > 0 ? (spent / b.getBudgetAmount()) * 100.0 : 0.0;
            boolean isWarning = pct >= b.getWarningThreshold();
            boolean isExceeded = spent > b.getBudgetAmount();

            item.put("budgetId", b.getBudgetId());
            item.put("userId", b.getUserId());
            item.put("category", b.getCategory());
            item.put("budgetAmount", b.getBudgetAmount());
            item.put("period", b.getPeriod());
            item.put("warningThreshold", b.getWarningThreshold());
            item.put("spentAmount", Math.round(spent * 100.0) / 100.0);
            item.put("percentageUsed", Math.round(pct * 10.0) / 10.0);
            item.put("isWarning", isWarning);
            item.put("isExceeded", isExceeded);
            item.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : null);
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Budget>> createBudget(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody BudgetRequest request) {

        Budget budget = new Budget(
                principal.getUserId(),
                request.getCategory().trim(),
                request.getBudgetAmount(),
                request.getPeriod() != null ? request.getPeriod().trim() : "MONTHLY",
                request.getWarningThreshold() != null ? request.getWarningThreshold() : 80.0
        );

        int id = budgetDAO.insert(budget);
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create budget", 500));
        }

        budget.setBudgetId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", budget));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Budget>> updateBudget(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id,
            @Valid @RequestBody BudgetRequest request) {

        Budget existing = budgetDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Budget not found", 404));
        }

        existing.setCategory(request.getCategory().trim());
        existing.setBudgetAmount(request.getBudgetAmount());
        if (request.getPeriod() != null) existing.setPeriod(request.getPeriod().trim());
        if (request.getWarningThreshold() != null) existing.setWarningThreshold(request.getWarningThreshold());

        boolean ok = budgetDAO.update(existing);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update budget", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id) {

        Budget existing = budgetDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Budget not found", 404));
        }

        boolean ok = budgetDAO.delete(id);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete budget", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }
}
