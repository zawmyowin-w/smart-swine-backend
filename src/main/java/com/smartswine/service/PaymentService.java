package com.smartswine.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.smartswine.dto.request.PaymentRequest;
import com.smartswine.model.Order;
import com.smartswine.model.Payment;
import com.smartswine.model.User;
import com.smartswine.model.enums.OrderStatus;
import com.smartswine.model.enums.PaymentStatus;
import com.smartswine.repository.OrderRepository;
import com.smartswine.repository.PaymentRepository;
import com.smartswine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public Payment createPayment(PaymentRequest req, Long userId) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String qrData = buildQrData(order, req);
        String qrCodeBase64 = generateQrCode(qrData);

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(req.getMethod())
                .qrCodeData(qrData)
                .qrCodeUrl("data:image/png;base64," + qrCodeBase64)
                .status(PaymentStatus.PENDING)
                .notes(req.getNotes())
                .build();

        Payment saved = paymentRepository.save(payment);
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(order);
        return saved;
    }

    @Transactional
    public Payment customerConfirmPayment(Long paymentId, String transactionRef, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(PaymentStatus.CUSTOMER_PAID);
        payment.setTransactionRef(transactionRef);
        payment.setPaidAt(LocalDateTime.now());
        auditService.log(userId, null, "CUSTOMER_PAID", "Payment", paymentId, null, transactionRef, null, null);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment financeVerify(Long paymentId, Long financeUserId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        User verifier = userRepository.findById(financeUserId).orElse(null);

        payment.setStatus(PaymentStatus.FINANCE_VERIFIED);
        payment.setVerifiedBy(verifier);
        payment.setVerifiedAt(LocalDateTime.now());

        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        auditService.log(financeUserId, verifier != null ? verifier.getUsername() : null,
                "FINANCE_VERIFIED", "Payment", paymentId, null, "VERIFIED", null, null);
        return paymentRepository.save(payment);
    }

    public List<Payment> findPendingVerification() {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.CUSTOMER_PAID);
    }

    private String buildQrData(Order order, PaymentRequest req) {
        return String.format("SMARTSWINE|ORDER:%s|AMOUNT:%s|METHOD:%s",
                order.getOrderNumber(), order.getTotalAmount(), req.getMethod());
    }

    private String generateQrCode(String data) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("QR Code generation failed", e);
        }
    }
}
