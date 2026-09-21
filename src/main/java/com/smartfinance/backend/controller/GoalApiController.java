package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.dto.GoalContributionRequest;
import com.smartfinance.backend.dto.GoalRequest;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.dao.GoalDAO;
import com.smartfinance.model.Goal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalApiController {

    private final GoalDAO goalDAO = new GoalDAO();

    @GetMapping
    public ResponseEntity<ApiResponse<List<Goal>>> getGoals(@CurrentUser UserPrincipal principal) {
        List<Goal> goals = goalDAO.findByUserId(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Goal>> createGoal(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody GoalRequest request) {

        LocalDate deadline = LocalDate.now().plusMonths(6);
        if (request.getDeadline() != null && !request.getDeadline().isBlank()) {
            try {
                deadline = LocalDate.parse(request.getDeadline().trim());
            } catch (Exception ignore) {}
        }

        Goal goal = new Goal(
                principal.getUserId(),
                request.getGoalName().trim(),
                request.getTargetAmount(),
                request.getSavedAmount() != null ? request.getSavedAmount() : 0.0,
                deadline,
                "ACTIVE",
                request.getPriority() != null ? request.getPriority() : "MEDIUM",
                request.getCategory() != null ? request.getCategory() : "General",
                request.getMonthlyContribution() != null ? request.getMonthlyContribution() : 0.0,
                request.getExpectedReturn() != null ? request.getExpectedReturn() : 0.0
        );

        int id = goalDAO.insert(goal);
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create financial goal", 500));
        }

        goal.setGoalId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created successfully", goal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Goal>> updateGoal(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id,
            @Valid @RequestBody GoalRequest request) {

        Goal existing = goalDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Goal not found", 404));
        }

        LocalDate deadline = existing.getDeadline();
        if (request.getDeadline() != null && !request.getDeadline().isBlank()) {
            try {
                deadline = LocalDate.parse(request.getDeadline().trim());
            } catch (Exception ignore) {}
        }

        existing.setGoalName(request.getGoalName().trim());
        existing.setTargetAmount(request.getTargetAmount());
        if (request.getSavedAmount() != null) existing.setSavedAmount(request.getSavedAmount());
        existing.setDeadline(deadline);
        if (request.getPriority() != null) existing.setPriority(request.getPriority());
        if (request.getCategory() != null) existing.setCategory(request.getCategory());
        if (request.getMonthlyContribution() != null) existing.setMonthlyContribution(request.getMonthlyContribution());
        if (request.getExpectedReturn() != null) existing.setExpectedReturn(request.getExpectedReturn());

        boolean ok = goalDAO.update(existing);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update goal", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Goal updated successfully", existing));
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<ApiResponse<Goal>> contributeToGoal(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id,
            @Valid @RequestBody GoalContributionRequest request) {

        Goal existing = goalDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Goal not found", 404));
        }

        double newSaved = existing.getSavedAmount() + request.getAmount();
        boolean ok = goalDAO.updateSavedAmount(id, newSaved);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add contribution", 500));
        }

        existing.setSavedAmount(newSaved);
        if (newSaved >= existing.getTargetAmount()) {
            existing.setStatus("COMPLETED");
            goalDAO.update(existing);
        }

        return ResponseEntity.ok(ApiResponse.success("Contribution recorded successfully", existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id) {

        Goal existing = goalDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Goal not found", 404));
        }

        boolean ok = goalDAO.delete(id);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete goal", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Goal deleted successfully", null));
    }
}
