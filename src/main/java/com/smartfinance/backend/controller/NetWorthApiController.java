package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.dto.AssetRequest;
import com.smartfinance.backend.dto.LiabilityRequest;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.dao.AssetDAO;
import com.smartfinance.dao.LiabilityDAO;
import com.smartfinance.model.Asset;
import com.smartfinance.model.Liability;
import com.smartfinance.service.NetWorthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NetWorthApiController {

    private final AssetDAO assetDAO = new AssetDAO();
    private final LiabilityDAO liabilityDAO = new LiabilityDAO();
    private final NetWorthService netWorthService = new NetWorthService();

    @GetMapping("/networth")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNetWorth(@CurrentUser UserPrincipal principal) {
        int userId = principal.getUserId();
        double totalAssets = netWorthService.getTotalAssets(userId);
        double totalLiabilities = netWorthService.getTotalLiabilities(userId);
        double netWorth = totalAssets - totalLiabilities;

        Map<String, Object> data = new HashMap<>();
        data.put("totalAssets", totalAssets);
        data.put("totalLiabilities", totalLiabilities);
        data.put("netWorth", netWorth);
        data.put("debtToAssetRatio", totalAssets > 0 ? Math.round((totalLiabilities / totalAssets) * 1000.0) / 10.0 : 0.0);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // --- Assets ---
    @GetMapping("/assets")
    public ResponseEntity<ApiResponse<List<Asset>>> getAssets(@CurrentUser UserPrincipal principal) {
        List<Asset> list = assetDAO.findByUserId(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/assets")
    public ResponseEntity<ApiResponse<Asset>> createAsset(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody AssetRequest request) {

        Asset asset = new Asset(
                principal.getUserId(),
                request.getName().trim(),
                request.getType().trim(),
                request.getValue(),
                request.getNotes() != null ? request.getNotes().trim() : ""
        );

        int id = assetDAO.insert(asset);
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add asset", 500));
        }

        asset.setAssetId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset added successfully", asset));
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id) {

        Asset existing = assetDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Asset not found", 404));
        }

        boolean ok = assetDAO.delete(id);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete asset", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Asset deleted successfully", null));
    }

    // --- Liabilities ---
    @GetMapping("/liabilities")
    public ResponseEntity<ApiResponse<List<Liability>>> getLiabilities(@CurrentUser UserPrincipal principal) {
        List<Liability> list = liabilityDAO.findByUserId(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/liabilities")
    public ResponseEntity<ApiResponse<Liability>> createLiability(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody LiabilityRequest request) {

        double rem = request.getRemainingBalance() != null ? request.getRemainingBalance() : request.getPrincipal();

        Liability liability = new Liability(
                principal.getUserId(),
                request.getName().trim(),
                request.getPrincipal(),
                request.getInterestRate() != null ? request.getInterestRate() : 0.0,
                request.getTenureMonths() != null ? request.getTenureMonths() : 12,
                request.getEmi() != null ? request.getEmi() : 0.0,
                rem,
                request.getDueDate() != null ? request.getDueDate() : 5
        );

        int id = liabilityDAO.insert(liability);
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add liability", 500));
        }

        liability.setLiabilityId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Liability added successfully", liability));
    }

    @DeleteMapping("/liabilities/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLiability(
            @CurrentUser UserPrincipal principal,
            @PathVariable("id") int id) {

        Liability existing = liabilityDAO.findById(id);
        if (existing == null || existing.getUserId() != principal.getUserId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Liability not found", 404));
        }

        boolean ok = liabilityDAO.delete(id);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete liability", 500));
        }

        return ResponseEntity.ok(ApiResponse.success("Liability deleted successfully", null));
    }
}
