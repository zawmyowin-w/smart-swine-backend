package com.smartswine.controller;

import com.smartswine.dto.request.BirthRequest;
import com.smartswine.dto.request.BreedingRequest;
import com.smartswine.dto.response.ApiResponse;
import com.smartswine.model.Birth;
import com.smartswine.model.BreedingRecord;
import com.smartswine.model.Pregnancy;
import com.smartswine.repository.UserRepository;
import com.smartswine.service.PigLifecycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lifecycle")
@RequiredArgsConstructor
public class PigLifecycleController {

    private final PigLifecycleService lifecycleService;
    private final UserRepository userRepository;

    @PostMapping("/breeding")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<BreedingRecord>> recordBreeding(
            @Valid @RequestBody BreedingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Breeding recorded", lifecycleService.recordBreeding(request, userId)));
    }

    @PostMapping("/pregnancy/confirm/{breedingRecordId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Pregnancy>> confirmPregnancy(
            @PathVariable Long breedingRecordId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Pregnancy confirmed",
                lifecycleService.confirmPregnancy(breedingRecordId, getUserId(userDetails))));
    }

    @GetMapping("/pregnancies/active")
    public ResponseEntity<ApiResponse<List<Pregnancy>>> getActivePregnancies() {
        return ResponseEntity.ok(ApiResponse.success(lifecycleService.getActivePregnancies()));
    }

    @PostMapping("/birth")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Birth>> recordBirth(
            @Valid @RequestBody BirthRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Birth recorded",
                lifecycleService.recordBirth(request, getUserId(userDetails))));
    }

    @GetMapping("/births/pending-hr")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<List<Birth>>> getPendingHrConfirmations() {
        return ResponseEntity.ok(ApiResponse.success(lifecycleService.getPendingHrConfirmations()));
    }

    @PostMapping("/births/{birthId}/hr-confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<Birth>> hrConfirmBirth(
            @PathVariable Long birthId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Birth confirmed by HR",
                lifecycleService.hrConfirmBirth(birthId, getUserId(userDetails))));
    }

    @PostMapping("/births/{birthId}/mark-available")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Birth>> markAvailable(
            @PathVariable Long birthId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Marked as available",
                lifecycleService.markBirthAvailable(birthId, getUserId(userDetails))));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).map(u -> u.getId()).orElse(null);
    }
}
