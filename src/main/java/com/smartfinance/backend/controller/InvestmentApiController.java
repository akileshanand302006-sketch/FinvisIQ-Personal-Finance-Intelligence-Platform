package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.dto.InvestmentRequest;
import com.smartfinance.backend.dto.SIPCalculationRequest;
import com.smartfinance.backend.dto.SIPCalculationResponse;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.dao.InvestmentDAO;
import com.smartfinance.model.Investment;
import com.smartfinance.service.SIPCalculatorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/investments")
public class InvestmentApiController {

    private final InvestmentDAO investmentDAO = new InvestmentDAO();
    private final SIPCalculatorService sipCalculator = new SIPCalculatorService();

    @GetMapping
    public ResponseEntity<ApiResponse<List<Investment>>> getInvestments(@CurrentUser UserPrincipal principal) {
        List<Investment> list = investmentDAO.findByUserId(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Investment>> createInvestment(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody InvestmentRequest request) {

        LocalDate startDate = LocalDate.now();
        if (request.getStartDate() != null && !request.getStartDate().isBlank()) {
            try {
                startDate = LocalDate.parse(request.getStartDate().trim());
            } catch (Exception ignore) {}
        }

        Investment investment = new Investment(
                principal.getUserId(),
                request.getType().trim(),
                request.getAmount(),
                request.getReturnRate() != null ? request.getReturnRate() : 8.0,
                startDate
        );

        int id = investmentDAO.insert(investment);
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add investment", 500));
        }

        investment.setInvestmentId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Investment added successfully", investment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvestment(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id) {

        Investment existing = investmentDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Investment not found", 404));
        }

        boolean ok = investmentDAO.delete(id);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete investment", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Investment deleted successfully", null));
    }

    @PostMapping("/sip-calculate")
    public ResponseEntity<ApiResponse<SIPCalculationResponse>> calculateSIP(
            @Valid @RequestBody SIPCalculationRequest request) {

        double maturity = sipCalculator.calculateSIPMaturity(
                request.getMonthlyInvestment(),
                request.getExpectedAnnualReturn(),
                request.getTenureYears()
        );
        double totalInvested = sipCalculator.calculateTotalInvested(
                request.getMonthlyInvestment(),
                request.getTenureYears()
        );
        double returns = maturity - totalInvested;

        SIPCalculationResponse resp = new SIPCalculationResponse(
                totalInvested,
                maturity,
                Math.round(returns * 100.0) / 100.0
        );

        return ResponseEntity.ok(ApiResponse.success("SIP calculation successful", resp));
    }
}
