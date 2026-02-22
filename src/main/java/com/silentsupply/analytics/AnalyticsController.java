package com.silentsupply.analytics;

import com.silentsupply.analytics.dto.BuyerDashboardResponse;
import com.silentsupply.analytics.dto.SupplierDashboardResponse;
import com.silentsupply.config.CompanyUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for analytics dashboard endpoints.
 * Provides aggregated metrics for suppliers and buyers.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard metrics for suppliers and buyers")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Returns the supplier analytics dashboard.
     *
     * @param userDetails the authenticated supplier
     * @return supplier dashboard metrics
     */
    @GetMapping("/supplier")
    @Operation(summary = "Get supplier analytics dashboard")
    public ResponseEntity<SupplierDashboardResponse> supplierDashboard(
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getSupplierDashboard(userDetails.getId()));
    }

    /**
     * Returns the buyer analytics dashboard.
     *
     * @param userDetails the authenticated buyer
     * @return buyer dashboard metrics
     */
    @GetMapping("/buyer")
    @Operation(summary = "Get buyer analytics dashboard")
    public ResponseEntity<BuyerDashboardResponse> buyerDashboard(
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getBuyerDashboard(userDetails.getId()));
    }
}
