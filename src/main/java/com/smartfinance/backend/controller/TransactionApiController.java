package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.dto.TransactionRequest;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.model.Transaction;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionApiController {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactions(
            @CurrentUser UserPrincipal principal,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search) {

        int userId = principal.getUserId();
        List<Transaction> list = transactionDAO.findByUserId(userId);

        if (type != null && !type.isBlank()) {
            list = list.stream()
                    .filter(t -> t.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        }

        if (category != null && !category.isBlank()) {
            list = list.stream()
                    .filter(t -> t.getCategory().equalsIgnoreCase(category.trim()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            list = list.stream()
                    .filter(t -> (t.getDescription() != null && t.getDescription().toLowerCase().contains(q))
                            || t.getCategory().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Transaction>> getTransactionById(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id) {

        Transaction txn = transactionDAO.findById(id);
        if (txn == null || txn.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Transaction not found", 404));
        }

        return ResponseEntity.ok(ApiResponse.success(txn));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> createTransaction(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody TransactionRequest request) {

        LocalDate date = LocalDate.now();
        if (request.getDate() != null && !request.getDate().isBlank()) {
            try {
                date = LocalDate.parse(request.getDate().trim());
            } catch (Exception ignore) {}
        }

        Transaction txn = new Transaction(
                principal.getUserId(),
                request.getAmount(),
                request.getType().trim().toUpperCase(),
                request.getCategory().trim(),
                date,
                request.getDescription() != null ? request.getDescription().trim() : "",
                request.getPaymentMethod() != null ? request.getPaymentMethod().trim() : "Cash"
        );

        int id = transactionDAO.insert(txn);
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create transaction", 500));
        }

        txn.setTransactionId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", txn));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Transaction>> updateTransaction(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id,
            @Valid @RequestBody TransactionRequest request) {

        Transaction existing = transactionDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Transaction not found", 404));
        }

        LocalDate date = existing.getDate();
        if (request.getDate() != null && !request.getDate().isBlank()) {
            try {
                date = LocalDate.parse(request.getDate().trim());
            } catch (Exception ignore) {}
        }

        existing.setAmount(request.getAmount());
        existing.setType(request.getType().trim().toUpperCase());
        existing.setCategory(request.getCategory().trim());
        existing.setDate(date);
        existing.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        existing.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().trim() : "Cash");

        boolean ok = transactionDAO.update(existing);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update transaction", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id) {

        Transaction existing = transactionDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Transaction not found", 404));
        }

        boolean ok = transactionDAO.delete(id);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete transaction", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully", null));
    }
}
