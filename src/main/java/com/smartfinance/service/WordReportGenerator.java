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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Pure Java OpenXML (.docx) Word Document Generator Engine — Generates 100% genuine,
 * native OpenXML Microsoft Word documents (.docx) containing executive metrics, visual category bar chart tables,
 * subscriptions, goals, and transaction ledger.
 */
public class WordReportGenerator {

    public static File generateWordDocument(int userId, String userName, String currencySymbol, ArrayList<Transaction> txns, File outputFile) throws IOException {
        BudgetDAO budgetDAO = new BudgetDAO();
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        GoalDAO goalDAO = new GoalDAO();

        ArrayList<Budget> budgets = budgetDAO.findByUserId(userId);
        ArrayList<Subscription> subscriptions = subscriptionDAO.findByUserId(userId);
        ArrayList<Goal> goals = goalDAO.findByUserId(userId);

        double totalIncome = txns.stream().filter(Transaction::isIncome).mapToDouble(Transaction::getAmount).sum();
        double totalExpenses = txns.stream().filter(Transaction::isExpense).mapToDouble(Transaction::getAmount).sum();
        double netSavings = totalIncome - totalExpenses;
        double savingsRate = totalIncome > 0 ? (netSavings / totalIncome) * 100.0 : 0;
        double monthlySubCost = subscriptions.stream().mapToDouble(Subscription::getAmount).sum();

        // Calculate Category Breakdown Analytics
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction t : txns) {
            if (t.isExpense()) {
                categoryExpenses.put(t.getCategory(), categoryExpenses.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        // Generate document.xml content
        StringBuilder docXml = new StringBuilder();
        docXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        docXml.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n");
        docXml.append("  <w:body>\n");

        // Title Paragraph
        docXml.append("    <w:p>\n");
        docXml.append("      <w:pPr><w:spacing w:after=\"120\"/></w:pPr>\n");
        docXml.append("      <w:r>\n");
        docXml.append("        <w:rPr><w:b/><w:sz w:val=\"44\"/><w:color w:val=\"7C3AED\"/></w:rPr>\n");
        docXml.append("        <w:t>FinvisIQ Executive Financial &amp; Analytical Report</w:t>\n");
        docXml.append("      </w:r>\n");
        docXml.append("    </w:p>\n");

        // Subtitle Paragraph
        docXml.append("    <w:p>\n");
        docXml.append("      <w:pPr><w:spacing w:after=\"360\"/></w:pPr>\n");
        docXml.append("      <w:r>\n");
        docXml.append("        <w:rPr><w:b/><w:sz w:val=\"22\"/><w:color w:val=\"1E293B\"/></w:rPr>\n");
        docXml.append("        <w:t>Account Holder: ").append(escapeXml(userName)).append("  |  Date: ").append(dateStr).append("</w:t>\n");
        docXml.append("      </w:r>\n");
        docXml.append("    </w:p>\n");

        // Section Title 1: Executive Summary
        docXml.append("    <w:p>\n");
        docXml.append("      <w:pPr><w:spacing w:after=\"180\"/></w:pPr>\n");
        docXml.append("      <w:r>\n");
        docXml.append("        <w:rPr><w:b/><w:sz w:val=\"28\"/><w:color w:val=\"0F172A\"/></w:rPr>\n");
        docXml.append("        <w:t>1. Executive Summary Metrics</w:t>\n");
        docXml.append("      </w:r>\n");
        docXml.append("    </w:p>\n");

        // Metrics Summary Table
        docXml.append("    <w:tbl>\n");
        docXml.append("      <w:tblPr>\n");
        docXml.append("        <w:tblW w:w=\"9000\" w:type=\"dxa\"/>\n");
        docXml.append("        <w:tblBorders>\n");
        docXml.append("          <w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("        </w:tblBorders>\n");
        docXml.append("      </w:tblPr>\n");

        docXml.append("      <w:tr>\n");
        appendTableCell(docXml, "Total Income", "F8FAFC", true, "64748B");
        appendTableCell(docXml, "Total Expenses", "F8FAFC", true, "64748B");
        appendTableCell(docXml, "Net Savings Balance", "F8FAFC", true, "64748B");
        appendTableCell(docXml, "Overall Savings Rate", "F8FAFC", true, "64748B");
        docXml.append("      </w:tr>\n");

        docXml.append("      <w:tr>\n");
        appendTableCell(docXml, ValidationUtils.formatCurrency(totalIncome), "FFFFFF", true, "059669");
        appendTableCell(docXml, ValidationUtils.formatCurrency(totalExpenses), "FFFFFF", true, "DC2626");
        appendTableCell(docXml, ValidationUtils.formatCurrency(netSavings), "FFFFFF", true, "7C3AED");
        appendTableCell(docXml, String.format("%.1f%%", savingsRate), "FFFFFF", true, "06B6D4");
        docXml.append("      </w:tr>\n");
        docXml.append("    </w:tbl>\n");

        docXml.append("    <w:p><w:pPr><w:spacing w:after=\"300\"/></w:pPr></w:p>\n");

        // Section Title 2: Visual Analytical Category Breakdown
        docXml.append("    <w:p>\n");
        docXml.append("      <w:pPr><w:spacing w:after=\"180\"/></w:pPr>\n");
        docXml.append("      <w:r>\n");
        docXml.append("        <w:rPr><w:b/><w:sz w:val=\"28\"/><w:color w:val=\"0F172A\"/></w:rPr>\n");
        docXml.append("        <w:t>2. Analytical Category Expense Breakdown &amp; Distribution</w:t>\n");
        docXml.append("      </w:r>\n");
        docXml.append("    </w:p>\n");

        // Category Visual Breakdown Table
        docXml.append("    <w:tbl>\n");
        docXml.append("      <w:tblPr>\n");
        docXml.append("        <w:tblW w:w=\"9000\" w:type=\"dxa\"/>\n");
        docXml.append("        <w:tblBorders>\n");
        docXml.append("          <w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"E2E8F0\"/>\n");
        docXml.append("          <w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"E2E8F0\"/>\n");
        docXml.append("        </w:tblBorders>\n");
        docXml.append("      </w:tblPr>\n");

        docXml.append("      <w:tr>\n");
        appendTableCell(docXml, "Category Name", "7C3AED", true, "FFFFFF");
        appendTableCell(docXml, "Amount Spent", "7C3AED", true, "FFFFFF");
        appendTableCell(docXml, "% Share", "7C3AED", true, "FFFFFF");
        appendTableCell(docXml, "Analytical Visual Callout Bar", "7C3AED", true, "FFFFFF");
        docXml.append("      </w:tr>\n");

        int catCount = 0;
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            double spent = entry.getValue();
            double pct = totalExpenses > 0 ? (spent / totalExpenses) * 100.0 : 0;
            String bg = (catCount++ % 2 == 1) ? "F8FAFC" : "FFFFFF";
            String barText = String.format("[ %s ]", "||||||||||||||||||||".substring(0, (int) Math.min(20, Math.max(1, (pct / 100.0) * 20))));

            docXml.append("      <w:tr>\n");
            appendTableCell(docXml, entry.getKey(), bg, false, "0F172A");
            appendTableCell(docXml, ValidationUtils.formatCurrency(spent), bg, true, "DC2626");
            appendTableCell(docXml, String.format("%.1f%%", pct), bg, true, "7C3AED");
            appendTableCell(docXml, barText + String.format(" (%.1f%%)", pct), "F1F5F9", true, "7C3AED");
            docXml.append("      </w:tr>\n");
        }
        docXml.append("    </w:tbl>\n");

        docXml.append("    <w:p><w:pPr><w:spacing w:after=\"300\"/></w:pPr></w:p>\n");

        // Section Title 3: Subscriptions & Goals Metrics
        docXml.append("    <w:p>\n");
        docXml.append("      <w:pPr><w:spacing w:after=\"180\"/></w:pPr>\n");
        docXml.append("      <w:r>\n");
        docXml.append("        <w:rPr><w:b/><w:sz w:val=\"28\"/><w:color w:val=\"0F172A\"/></w:rPr>\n");
        docXml.append("        <w:t>3. Subscriptions &amp; Financial Goals Analytics</w:t>\n");
        docXml.append("      </w:r>\n");
        docXml.append("    </w:p>\n");

        docXml.append("    <w:p>\n");
        docXml.append("      <w:pPr><w:spacing w:after=\"140\"/></w:pPr>\n");
        docXml.append("      <w:r>\n");
        docXml.append("        <w:rPr><w:sz w:val=\"20\"/><w:color w:val=\"1E293B\"/></w:rPr>\n");
        docXml.append("        <w:t>Active Subscriptions Tracked: ").append(subscriptions.size()).append("  |  Total Monthly Subscription Commitment: ").append(escapeXml(ValidationUtils.formatCurrency(monthlySubCost))).append("</w:t>\n");
        docXml.append("      </w:r>\n");
        docXml.append("    </w:p>\n");

        docXml.append("    <w:p>\n");
        docXml.append("      <w:pPr><w:spacing w:after=\"300\"/></w:pPr>\n");
        docXml.append("      <w:r>\n");
        docXml.append("        <w:rPr><w:sz w:val=\"20\"/><w:color w:val=\"1E293B\"/></w:rPr>\n");
        docXml.append("        <w:t>Configured Category Budgets: ").append(budgets.size()).append("  |  Active Financial Target Goals: ").append(goals.size()).append("</w:t>\n");
        docXml.append("      </w:r>\n");
        docXml.append("    </w:p>\n");

        // Section Title 4: Ledger Table
        docXml.append("    <w:p>\n");
        docXml.append("      <w:pPr><w:spacing w:after=\"180\"/></w:pPr>\n");
        docXml.append("      <w:r>\n");
        docXml.append("        <w:rPr><w:b/><w:sz w:val=\"28\"/><w:color w:val=\"0F172A\"/></w:rPr>\n");
        docXml.append("        <w:t>4. Detailed Transaction Ledger</w:t>\n");
        docXml.append("      </w:r>\n");
        docXml.append("    </w:p>\n");

        // Transaction Table
        docXml.append("    <w:tbl>\n");
        docXml.append("      <w:tblPr>\n");
        docXml.append("        <w:tblW w:w=\"9000\" w:type=\"dxa\"/>\n");
        docXml.append("        <w:tblBorders>\n");
        docXml.append("          <w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>\n");
        docXml.append("          <w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"E2E8F0\"/>\n");
        docXml.append("          <w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"E2E8F0\"/>\n");
        docXml.append("        </w:tblBorders>\n");
        docXml.append("      </w:tblPr>\n");

        docXml.append("      <w:tr>\n");
        appendTableCell(docXml, "Date", "7C3AED", true, "FFFFFF");
        appendTableCell(docXml, "Type", "7C3AED", true, "FFFFFF");
        appendTableCell(docXml, "Category", "7C3AED", true, "FFFFFF");
        appendTableCell(docXml, "Amount", "7C3AED", true, "FFFFFF");
        appendTableCell(docXml, "Method", "7C3AED", true, "FFFFFF");
        appendTableCell(docXml, "Description", "7C3AED", true, "FFFFFF");
        docXml.append("      </w:tr>\n");

        int count = 0;
        for (Transaction t : txns) {
            String bg = (count++ % 2 == 1) ? "F8FAFC" : "FFFFFF";
            String amtColor = t.isIncome() ? "059669" : "DC2626";

            docXml.append("      <w:tr>\n");
            appendTableCell(docXml, t.getDate().toString(), bg, false, "0F172A");
            appendTableCell(docXml, t.getType(), bg, true, amtColor);
            appendTableCell(docXml, t.getCategory(), bg, false, "0F172A");
            appendTableCell(docXml, ValidationUtils.formatCurrency(t.getAmount()), bg, true, amtColor);
            appendTableCell(docXml, t.getPaymentMethod(), bg, false, "0F172A");
            appendTableCell(docXml, t.getDescription() != null ? t.getDescription() : "-", bg, false, "64748B");
            docXml.append("      </w:tr>\n");
        }

        docXml.append("    </w:tbl>\n");

        // Footer Note
        docXml.append("    <w:p>\n");
        docXml.append("      <w:pPr><w:spacing w:before=\"400\"/></w:pPr>\n");
        docXml.append("      <w:r>\n");
        docXml.append("        <w:rPr><w:i/><w:sz w:val=\"18\"/><w:color w:val=\"94A3B8\"/></w:rPr>\n");
        docXml.append("        <w:t>FinvisIQ Personal Finance Intelligence System - Confidential Executive Analytical Statement</w:t>\n");
        docXml.append("      </w:r>\n");
        docXml.append("    </w:p>\n");

        docXml.append("  </w:body>\n");
        docXml.append("</w:document>");

        // Package into genuine OpenXML .docx ZIP archive
        try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write(getContentTypesXml().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("_rels/.rels"));
            zip.write(getRelsXml().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("word/document.xml"));
            zip.write(docXml.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        return outputFile;
    }

    private static void appendTableCell(StringBuilder sb, String text, String bgColor, boolean isBold, String textColor) {
        sb.append("        <w:tc>\n");
        sb.append("          <w:tcPr>\n");
        sb.append("            <w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"").append(bgColor).append("\"/>\n");
        sb.append("            <w:tcMar>\n");
        sb.append("              <w:top w:w=\"120\" w:type=\"dxa\"/>\n");
        sb.append("              <w:bottom w:w=\"120\" w:type=\"dxa\"/>\n");
        sb.append("              <w:left w:w=\"160\" w:type=\"dxa\"/>\n");
        sb.append("              <w:right w:w=\"160\" w:type=\"dxa\"/>\n");
        sb.append("            </w:tcMar>\n");
        sb.append("          </w:tcPr>\n");
        sb.append("          <w:p>\n");
        sb.append("            <w:r>\n");
        sb.append("              <w:rPr>\n");
        if (isBold) sb.append("                <w:b/>\n");
        sb.append("                <w:sz w:val=\"20\"/>\n");
        sb.append("                <w:color w:val=\"").append(textColor).append("\"/>\n");
        sb.append("              </w:rPr>\n");
        sb.append("              <w:t>").append(escapeXml(text)).append("</w:t>\n");
        sb.append("            </w:r>\n");
        sb.append("          </w:p>\n");
        sb.append("        </w:tc>\n");
    }

    private static String getContentTypesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
               "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
               "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
               "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
               "  <Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>\n" +
               "</Types>";
    }

    private static String getRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
               "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
               "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>\n" +
               "</Relationships>";
    }

    private static String escapeXml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}
