package com.smartfinance.service;

import com.smartfinance.dao.AssetDAO;
import com.smartfinance.dao.LiabilityDAO;
import com.smartfinance.model.Asset;
import com.smartfinance.model.Liability;

import java.util.ArrayList;

/**
 * Net Worth Service — Calculates Total Assets, Total Liabilities, and Net Worth.
 */
public class NetWorthService {
    private final AssetDAO assetDAO;
    private final LiabilityDAO liabilityDAO;

    public NetWorthService() {
        this.assetDAO = new AssetDAO();
        this.liabilityDAO = new LiabilityDAO();
    }

    public double getTotalAssets(int userId) {
        ArrayList<Asset> assets = assetDAO.findByUserId(userId);
        double total = 0;
        for (Asset a : assets) {
            total += a.getValue();
        }
        return total;
    }

    public double getTotalLiabilities(int userId) {
        ArrayList<Liability> liabilities = liabilityDAO.findByUserId(userId);
        double total = 0;
        for (Liability l : liabilities) {
            total += l.getRemainingBalance();
        }
        return total;
    }

    public double getNetWorth(int userId) {
        return getTotalAssets(userId) - getTotalLiabilities(userId);
    }
}
