package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.model.FinancialInsight;
import com.smartfinance.service.AIAdvisorService;
import com.smartfinance.service.InsightEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIAdvisorApiController {

    private final AIAdvisorService aiAdvisorService = new AIAdvisorService();
    private final InsightEngine insightEngine = new InsightEngine();

    @GetMapping("/insights")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInsights(@CurrentUser UserPrincipal principal) {
        int userId = principal.getUserId();

        ArrayList<String> suggestions = aiAdvisorService.generateSuggestions(userId);
        ArrayList<FinancialInsight> insights = insightEngine.generateInsights(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("suggestions", suggestions);
        data.put("insights", insights);

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
