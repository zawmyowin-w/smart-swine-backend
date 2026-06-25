package com.smartswine.controller;

import com.smartswine.dto.request.PigRequest;
import com.smartswine.dto.response.ApiResponse;
import com.smartswine.model.Pig;
import com.smartswine.model.enums.PigStatus;
import com.smartswine.repository.UserRepository;
import com.smartswine.service.PigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pigs")
@RequiredArgsConstructor
public class PigController {

    private final PigService pigService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Pig>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(pigService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Pig>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(pigService.findById(id)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Pig>>> findByStatus(@PathVariable PigStatus status) {
        return ResponseEntity.ok(ApiResponse.success(pigService.findByStatus(status)));
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<ApiResponse<List<Pig>>> findByFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(ApiResponse.success(pigService.findByFarm(farmId)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(pigService.getStats()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'SUPERVISOR', 'STAFF')")
    public ResponseEntity<ApiResponse<Pig>> create(
            @Valid @RequestBody PigRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Pig created", pigService.create(request, userId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Pig>> update(
            @PathVariable Long id,
            @Valid @RequestBody PigRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Pig updated", pigService.update(id, request, userId)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam PigStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        pigService.updateStatus(id, status, getUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Status updated", null));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId()).orElse(null);
    }
}
