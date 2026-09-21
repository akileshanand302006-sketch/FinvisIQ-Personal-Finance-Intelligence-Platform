package com.smartfinance.service;

import com.smartfinance.dao.BudgetDAO;
import com.smartfinance.dao.GoalDAO;
import com.smartfinance.dao.SubscriptionDAO;
import com.smartfinance.model.Budget;
import com.smartfinance.model.Goal;
import com.smartfinance.model.Subscription;
import com.smartfinance.model.Transaction;
import com.smartfinance.util.ValidationUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Pure Java PDF 1.4 Generator Engine — Creates 100% valid binary PDF files readable natively
 * by Adobe Acrobat, Chrome, Edge, Preview, and all standard PDF viewers without external libraries.
 * Uses exact absolute text matrix positioning (1 0 0 1 X Y Tm) for 100% pixel-perfect column alignment.
 */
public class PdfReportGenerator {

    public static File generatePDF(int userId, String userName, String currencySymbol, ArrayList<Transaction> txns, File outputFile) throws IOException {
        BudgetDAO budgetDAO = new BudgetDAO();
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        GoalDAO goalDAO = new GoalDAO();

        ArrayList<Budget> budgets = budgetDAO.findByUserId(userId);
        ArrayList<Subscription> subscriptions = subscriptionDAO.findByUserId(userId);
        ArrayList<Goal> goals = goalDAO.findByUserId(userId);

        double totalIncome = txns.stream().filter(t -> t != null && t.isIncome()).mapToDouble(t -> t.getAmount()).sum();
        double totalExpenses = txns.stream().filter(t -> t != null && t.isExpense()).mapToDouble(t -> t.getAmount()).sum();
        double netSavings = totalIncome - totalExpenses;
        double savingsRate = totalIncome > 0 ? (netSavings / totalIncome) * 100.0 : 0;
        double monthlySubCost = subscriptions.stream().filter(s -> s != null).mapToDouble(s -> s.getAmount()).sum();

        // Calculate Category Breakdown Analytics
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction t : txns) {
            if (t.isExpense()) {
                categoryExpenses.put(t.getCategory(), categoryExpenses.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }

        StringBuilder content = new StringBuilder();

        // Page Header Banner (Purple #7C3AED)
        content.append("0.486 0.227 0.929 rg\n");
        content.append("40 735 515 50 re f\n");

        // Header Title
        content.append("BT\n");
        content.append("/F2 18 Tf\n");
        content.append("1.0 1.0 1.0 rg\n");
        content.append("1 0 0 1 55 753 Tm\n");
        content.append("(FINVISIQ EXECUTIVE FINANCIAL & ANALYTICAL REPORT) Tj\n");
        content.append("ET\n");

        // Metadata Section
        content.append("BT\n");
        content.append("/F1 10 Tf\n");
        content.append("0.06 0.09 0.16 rg\n");
        content.append("1 0 0 1 40 710 Tm (Account Holder: ").append(sanitizePdfText(userName)).append(") Tj\n");
        content.append("1 0 0 1 40 696 Tm (Generated Date: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append(") Tj\n");
        content.append("1 0 0 1 40 682 Tm (Currency Symbol: ").append(sanitizePdfText(currencySymbol)).append(") Tj\n");
        content.append("ET\n");

        // Executive Summary Metrics Card Box
        content.append("0.97 0.98 0.99 rg\n");
        content.append("40 615 515 52 re f\n");
        content.append("0.8 0.85 0.9 RG\n");
        content.append("40 615 515 52 re s\n");

        // Executive Summary Metrics Labels & Values (Pixel-Perfect Alignment)
        content.append("BT\n");
        // Column 1: Total Income
        content.append("/F2 9 Tf 0.02 0.58 0.41 rg\n");
        content.append("1 0 0 1 55 648 Tm (TOTAL INCOME) Tj\n");
        content.append("/F1 11 Tf\n");
        content.append("1 0 0 1 55 632 Tm (").append(sanitizePdfText(ValidationUtils.formatCurrency(totalIncome))).append(") Tj\n");

        // Column 2: Total Expenses
        content.append("/F2 9 Tf 0.86 0.15 0.15 rg\n");
        content.append("1 0 0 1 180 648 Tm (TOTAL EXPENSES) Tj\n");
        content.append("/F1 11 Tf\n");
        content.append("1 0 0 1 180 632 Tm (").append(sanitizePdfText(ValidationUtils.formatCurrency(totalExpenses))).append(") Tj\n");

        // Column 3: Net Savings
        content.append("/F2 9 Tf 0.48 0.22 0.92 rg\n");
        content.append("1 0 0 1 310 648 Tm (NET SAVINGS) Tj\n");
        content.append("/F1 11 Tf\n");
        content.append("1 0 0 1 310 632 Tm (").append(sanitizePdfText(ValidationUtils.formatCurrency(netSavings))).append(") Tj\n");

        // Column 4: Savings Rate
        content.append("/F2 9 Tf 0.02 0.71 0.83 rg\n");
        content.append("1 0 0 1 435 648 Tm (SAVINGS RATE) Tj\n");
        content.append("/F1 11 Tf\n");
        content.append("1 0 0 1 435 632 Tm (").append(String.format("%.1f%%", savingsRate)).append(") Tj\n");
        content.append("ET\n");

        // Section Title 1: Category Expense Breakdown
        content.append("BT\n");
        content.append("/F2 12 Tf\n");
        content.append("0.06 0.09 0.16 rg\n");
        content.append("1 0 0 1 40 590 Tm (ANALYTICAL CATEGORY EXPENSE BREAKDOWN & VISUAL CHART) Tj\n");
        content.append("ET\n");

        // Category Table Header Box
        content.append("0.94 0.95 0.98 rg\n");
        content.append("40 562 515 20 re f\n");
        content.append("BT\n");
        content.append("/F2 9 Tf 0.1 0.1 0.1 rg\n");
        content.append("1 0 0 1 45 568 Tm (Category) Tj\n");
        content.append("1 0 0 1 175 568 Tm (Total Spent) Tj\n");
        content.append("1 0 0 1 260 568 Tm (% Share) Tj\n");
        content.append("1 0 0 1 345 568 Tm (Visual Distribution Chart Bar) Tj\n");
        content.append("ET\n");

        int yPos = 544;
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            double spent = entry.getValue();
            double pct = totalExpenses > 0 ? (spent / totalExpenses) * 100.0 : 0;
            int barWidth = (int) Math.min(200, (pct / 100.0) * 200);

            // Draw Category Text Values
            content.append("BT\n");
            content.append("/F1 9 Tf 0.1 0.1 0.1 rg\n");
            content.append("1 0 0 1 45 ").append(yPos).append(" Tm (").append(sanitizePdfText(trunc(entry.getKey(), 20))).append(") Tj\n");
            content.append("1 0 0 1 175 ").append(yPos).append(" Tm (").append(sanitizePdfText(ValidationUtils.formatCurrency(spent))).append(") Tj\n");
            content.append("1 0 0 1 260 ").append(yPos).append(" Tm (").append(String.format("%.1f%%", pct)).append(") Tj\n");
            content.append("ET\n");

            // Draw Visual Chart Bar (Background Track & Filled Bar)
            content.append("0.9 0.9 0.95 rg\n");
            content.append("345 ").append(yPos - 2).append(" 200 10 re f\n");
            if (barWidth > 0) {
                content.append("0.486 0.227 0.929 rg\n");
                content.append("345 ").append(yPos - 2).append(" ").append(barWidth).append(" 10 re f\n");
            }

            yPos -= 18;
            if (yPos < 430) break;
        }

        // Section Title 2: Subscriptions & Goals Summary
        yPos -= 10;
        content.append("BT\n");
        content.append("/F2 12 Tf 0.06 0.09 0.16 rg\n");
        content.append("1 0 0 1 40 ").append(yPos).append(" Tm (RECURRING SUBSCRIPTIONS & FINANCIAL GOALS METRICS) Tj\n");
        content.append("ET\n");

        yPos -= 18;
        content.append("BT\n");
        content.append("/F1 9.5 Tf 0.2 0.2 0.2 rg\n");
        content.append("1 0 0 1 40 ").append(yPos).append(" Tm (Active Subscriptions: ").append(subscriptions.size()).append("  |  Monthly Bill Commitment: ").append(sanitizePdfText(ValidationUtils.formatCurrency(monthlySubCost))).append(") Tj\n");
        yPos -= 14;
        content.append("1 0 0 1 40 ").append(yPos).append(" Tm (Configured Category Budgets: ").append(budgets.size()).append("  |  Tracked Financial Goals: ").append(goals.size()).append(") Tj\n");
        content.append("ET\n");

        // Section Title 3: Detailed Transaction Ledger Table
        yPos -= 26;
        content.append("0.486 0.227 0.929 rg\n");
        content.append("40 ").append(yPos).append(" 515 20 re f\n");

        content.append("BT\n");
        content.append("/F2 9 Tf 1.0 1.0 1.0 rg\n");
        content.append("1 0 0 1 45 ").append(yPos + 5).append(" Tm (Date) Tj\n");
        content.append("1 0 0 1 120 ").append(yPos + 5).append(" Tm (Type) Tj\n");
        content.append("1 0 0 1 185 ").append(yPos + 5).append(" Tm (Category) Tj\n");
        content.append("1 0 0 1 300 ").append(yPos + 5).append(" Tm (Amount) Tj\n");
        content.append("1 0 0 1 385 ").append(yPos + 5).append(" Tm (Method) Tj\n");
        content.append("1 0 0 1 470 ").append(yPos + 5).append(" Tm (Description) Tj\n");
        content.append("ET\n");

        // Transaction Data Rows
        yPos -= 18;
        int maxRows = Math.min(txns.size(), 16);

        for (int i = 0; i < maxRows; i++) {
            Transaction t = txns.get(i);

            // Row background stripe
            if (i % 2 == 1) {
                content.append("0.97 0.98 0.99 rg\n");
                content.append("40 ").append(yPos - 3).append(" 515 16 re f\n");
            }

            // Row bottom grid line
            content.append("0.9 0.92 0.94 RG\n");
            content.append("40 ").append(yPos - 3).append(" m 555 ").append(yPos - 3).append(" l s\n");

            String amtColor = t.isIncome() ? "0.02 0.58 0.41 rg\n" : "0.86 0.15 0.15 rg\n";

            content.append("BT\n");
            content.append("/F1 8.5 Tf 0.1 0.1 0.1 rg\n");
            content.append("1 0 0 1 45 ").append(yPos).append(" Tm (").append(sanitizePdfText(t.getDate().toString())).append(") Tj\n");

            content.append(amtColor);
            content.append("1 0 0 1 120 ").append(yPos).append(" Tm (").append(sanitizePdfText(t.getType())).append(") Tj\n");

            content.append("0.1 0.1 0.1 rg\n");
            content.append("1 0 0 1 185 ").append(yPos).append(" Tm (").append(sanitizePdfText(trunc(t.getCategory(), 18))).append(") Tj\n");

            content.append(amtColor);
            content.append("1 0 0 1 300 ").append(yPos).append(" Tm (").append(sanitizePdfText(ValidationUtils.formatCurrency(t.getAmount()))).append(") Tj\n");

            content.append("0.1 0.1 0.1 rg\n");
            content.append("1 0 0 1 385 ").append(yPos).append(" Tm (").append(sanitizePdfText(trunc(t.getPaymentMethod(), 12))).append(") Tj\n");
            content.append("1 0 0 1 470 ").append(yPos).append(" Tm (").append(sanitizePdfText(trunc(t.getDescription() != null ? t.getDescription() : "-", 14))).append(") Tj\n");
            content.append("ET\n");

            yPos -= 16;
            if (yPos < 45) break;
        }

        // Footer Note
        content.append("BT\n");
        content.append("/F1 8 Tf 0.5 0.5 0.5 rg\n");
        content.append("1 0 0 1 40 25 Tm (FinvisIQ Personal Finance Intelligence System - Executive Analytical Report) Tj\n");
        content.append("ET\n");

        byte[] streamBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);

        // Build 100% Valid PDF 1.4 Object Graph
        ArrayList<String> objects = new ArrayList<>();
        objects.add("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        objects.add("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        objects.add("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>\nendobj\n");
        objects.add("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        objects.add("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

        String streamObj = "6 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n" + content.toString() + "\nendstream\nendobj\n";
        objects.add(streamObj);

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));

            long[] offsets = new long[objects.size() + 1];
            long currentOffset = "%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1).length;

            for (int i = 0; i < objects.size(); i++) {
                offsets[i + 1] = currentOffset;
                byte[] objBytes = objects.get(i).getBytes(StandardCharsets.ISO_8859_1);
                out.write(objBytes);
                currentOffset += objBytes.length;
            }

            long xrefOffset = currentOffset;
            StringBuilder xref = new StringBuilder();
            xref.append("xref\n");
            xref.append("0 ").append(objects.size() + 1).append("\n");
            xref.append("0000000000 65535 f \n");

            for (int i = 1; i <= objects.size(); i++) {
                xref.append(String.format("%010d 00000 n \n", offsets[i]));
            }

            xref.append("trailer\n");
            xref.append("<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
            xref.append("startxref\n");
            xref.append(xrefOffset).append("\n");
            xref.append("%%EOF\n");

            out.write(xref.toString().getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
        }

        return outputFile;
    }

    private static String sanitizePdfText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replaceAll("[^\\x20-\\x7E]", " ");
    }

    private static String trunc(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 1) + ".";
    }
}
