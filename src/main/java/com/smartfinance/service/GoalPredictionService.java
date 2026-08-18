package com.smartfinance.service;

import com.smartfinance.model.Goal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Goal Prediction Service — Goal feasibility, monthly requirement & risk assessment.
 */
public class GoalPredictionService {

    public double getRequiredMonthlySaving(Goal goal) {
        if (goal.getDeadline() == null || goal.getDeadline().isBefore(LocalDate.now())) return 0;
        long months = ChronoUnit.MONTHS.between(LocalDate.now(), goal.getDeadline());
        if (months <= 0) months = 1;

        double remaining = goal.getTargetAmount() - goal.getSavedAmount();
        if (remaining <= 0) return 0;
        return Math.round((remaining / months) * 100.0) / 100.0;
    }

    /** Feasibility Risk Level: LOW, MEDIUM, HIGH, CRITICAL */
    public String getGoalRiskLevel(Goal goal, double availableMonthlySavings) {
        double required = getRequiredMonthlySaving(goal);
        if (required <= 0) return "LOW";
        if (availableMonthlySavings <= 0) return "HIGH";

        double ratio = required / availableMonthlySavings;
        if (ratio <= 0.4) return "LOW";
        if (ratio <= 0.8) return "MEDIUM";
        if (ratio <= 1.2) return "HIGH";
        return "CRITICAL";
    }
}
