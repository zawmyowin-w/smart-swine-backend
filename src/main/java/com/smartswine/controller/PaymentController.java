package com.smartswine.controller;

import com.smartswine.dto.request.PaymentRequest;
import com.smartswine.dto.response.ApiResponse;
import com.smartswine.model.Payment;
import com.smartswine.repository.UserRepository;
import com.smartswine.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Payment>> createPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Payment initiated", paymentService.createPayment(request, userId)));
    }

    @PostMapping("/{paymentId}/confirm")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Payment>> customerConfirm(
            @PathVariable Long paymentId,
            @RequestParam String transactionRef,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed",
                paymentService.customerConfirmPayment(paymentId, transactionRef, getUserId(userDetails))));
    }

    @GetMapping("/pending-verification")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Payment>>> pendingVerification() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.findPendingVerification()));
    }

    @PostMapping("/{paymentId}/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<Payment>> financeVerify(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Payment verified",
                paymentService.financeVerify(paymentId, getUserId(userDetails))));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).map(u -> u.getId()).orElse(null);
    }
}
