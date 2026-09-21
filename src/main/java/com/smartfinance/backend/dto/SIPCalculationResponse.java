package com.smartfinance.backend.dto;

public class SIPCalculationResponse {
    private double totalInvested;
    private double expectedMaturity;
    private double wealthGained;

    public SIPCalculationResponse() {}

    public SIPCalculationResponse(double totalInvested, double expectedMaturity, double wealthGained) {
        this.totalInvested = totalInvested;
        this.expectedMaturity = expectedMaturity;
        this.wealthGained = wealthGained;
    }

    public double getTotalInvested() { return totalInvested; }
    public void setTotalInvested(double totalInvested) { this.totalInvested = totalInvested; }

    public double getExpectedMaturity() { return expectedMaturity; }
    public void setExpectedMaturity(double expectedMaturity) { this.expectedMaturity = expectedMaturity; }

    public double getWealthGained() { return wealthGained; }
    public void setWealthGained(double wealthGained) { this.wealthGained = wealthGained; }
}
