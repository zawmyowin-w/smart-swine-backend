package com.smartswine.controller;

import com.smartswine.dto.response.ApiResponse;
import com.smartswine.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/super-admin")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSuperAdminDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSuperAdminDashboard()));
    }

    @GetMapping("/pig-stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPigDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getPigDashboard()));
    }

    @GetMapping("/finance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFinanceDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getFinanceDashboard()));
    }
}
