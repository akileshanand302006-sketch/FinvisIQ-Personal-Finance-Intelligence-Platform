package com.smartfinance.service;

import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Report Service — Bridge for exporting executive financial statements to valid PDF and Word documents.
 */
public class ReportService {
    private final TransactionDAO transactionDAO;

    public ReportService() {
        this.transactionDAO = new TransactionDAO();
    }

    /**
     * Export user transactions and financial summary to a valid PDF binary document (.pdf).
     */
    public File exportReportToPDF(int userId, String userName, String currencySymbol, File targetFile) throws IOException {
        ArrayList<Transaction> txns = transactionDAO.findByUserId(userId);
        return PdfReportGenerator.generatePDF(userId, userName, currencySymbol, txns, targetFile);
    }

    /**
     * Export user transactions and summary to a valid Microsoft Word document (.doc / .docx / .rtf).
     */
    public File exportReportToWord(int userId, String userName, String currencySymbol, File targetFile) throws IOException {
        ArrayList<Transaction> txns = transactionDAO.findByUserId(userId);
        return WordReportGenerator.generateWordDocument(userId, userName, currencySymbol, txns, targetFile);
    }
}
