package com.smartfinance.service;

import com.smartfinance.dao.SubscriptionDAO;
import com.smartfinance.model.Subscription;

import java.util.ArrayList;

/**
 * Subscription Service — Calculates total subscription commitment and tracks renewals.
 */
public class SubscriptionService {
    private final SubscriptionDAO subscriptionDAO;

    public SubscriptionService() {
        this.subscriptionDAO = new SubscriptionDAO();
    }

    public ArrayList<Subscription> getUserSubscriptions(int userId) {
        return subscriptionDAO.findByUserId(userId);
    }

    public double getTotalMonthlyCost(int userId) {
        ArrayList<Subscription> subs = subscriptionDAO.findByUserId(userId);
        double total = 0;
        for (Subscription sub : subs) {
            if (sub.isActive()) {
                total += sub.getMonthlyCost();
            }
        }
        return total;
    }

    public double getTotalYearlyCost(int userId) {
        return getTotalMonthlyCost(userId) * 12.0;
    }
}
