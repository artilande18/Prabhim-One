package com.example.registeration.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.registeration.dto.PaymentRequest;
import com.example.registeration.dto.PaymentResponse;
import com.example.registeration.entity.Customer;
import com.example.registeration.entity.Invoice;
import com.example.registeration.entity.Payment;
import com.example.registeration.entity.User;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.CustomerRepository;
import com.example.registeration.repository.InvoiceRepository;
import com.example.registeration.repository.PaymentRepository;
import com.example.registeration.repository.UserRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    private void validateUserExists(UUID userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments(UUID userId, UUID invoiceId, UUID customerId) {
        validateUserExists(userId);

        List<Payment> payments;
        if (invoiceId != null) {
            payments = paymentRepository.findByInvoiceIdAndIsDeletedFalseOrderByCreatedAtDesc(invoiceId);
        } else if (customerId != null) {
            payments = paymentRepository.findByCustomerIdAndIsDeletedFalseOrderByCreatedAtDesc(customerId);
        } else {
            payments = paymentRepository.findByIsDeletedFalseOrderByCreatedAtDesc();
        }

        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID id, UUID userId) {
        validateUserExists(userId);

        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        return mapToPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request, UUID userId) {
        validateUserExists(userId);

        if (request.getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + request.getInvoiceId()));

        BigDecimal invoiceGrandTotal = invoice.getGrandTotal() != null ? invoice.getGrandTotal() : BigDecimal.ZERO;

        List<Payment> existingPayments = paymentRepository
                .findByInvoiceIdAndIsDeletedFalseOrderByCreatedAtDesc(invoice.getId());

        BigDecimal currentTotalPaid = existingPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingBalanceBefore = invoiceGrandTotal.subtract(currentTotalPaid);

        if (remainingBalanceBefore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invoice is already fully paid.");
        }

        if (request.getAmount().compareTo(remainingBalanceBefore) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Payment amount (%.2f) exceeds invoice remaining balance (%.2f)",
                    request.getAmount(), remainingBalanceBefore
            ));
        }

        long count = paymentRepository.count();
        String paymentNumber = String.format("PAY-%06d", count + 1);

        Payment payment = new Payment();
        payment.setPaymentNumber(paymentNumber);
        payment.setInvoiceId(invoice.getId());
        payment.setCustomerId(invoice.getCustomerId());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setPaymentMode(request.getPaymentMode() != null && !request.getPaymentMode().trim().isEmpty() 
                ? request.getPaymentMode().trim() : "UPI");
        payment.setAmount(request.getAmount());
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setNotes(request.getNotes());
        payment.setCreatedBy(userId);
        payment.setOrganization(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
        payment.setIsDeleted(false);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Update Invoice status
        BigDecimal newTotalPaid = currentTotalPaid.add(request.getAmount());
        if (newTotalPaid.compareTo(invoiceGrandTotal) >= 0) {
            invoice.setStatus("paid");
        } else {
            invoice.setStatus("partially_paid");
        }
        invoice.setUpdatedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        return mapToPaymentResponse(savedPayment, invoice, newTotalPaid);
    }

    @Transactional
    public void deletePayment(UUID id, UUID userId) {
        validateUserExists(userId);

        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        payment.setIsDeleted(true);
        payment.setDeletedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Reconcile Invoice status
        if (payment.getInvoiceId() != null) {
            invoiceRepository.findById(payment.getInvoiceId())
                    .filter(i -> !i.isDeleted())
                    .ifPresent(invoice -> {
                        List<Payment> remainingPayments = paymentRepository
                                .findByInvoiceIdAndIsDeletedFalseOrderByCreatedAtDesc(invoice.getId());

                        BigDecimal remainingTotalPaid = remainingPayments.stream()
                                .map(Payment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal grandTotal = invoice.getGrandTotal() != null ? invoice.getGrandTotal() : BigDecimal.ZERO;

                        if (remainingTotalPaid.compareTo(BigDecimal.ZERO) == 0) {
                            invoice.setStatus("sent");
                        } else if (remainingTotalPaid.compareTo(grandTotal) >= 0) {
                            invoice.setStatus("paid");
                        } else {
                            invoice.setStatus("partially_paid");
                        }
                        invoice.setUpdatedAt(LocalDateTime.now());
                        invoiceRepository.save(invoice);
                    });
        }
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        Invoice invoice = null;
        if (payment.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
        }

        BigDecimal totalPaid = BigDecimal.ZERO;
        if (invoice != null) {
            List<Payment> payments = paymentRepository
                    .findByInvoiceIdAndIsDeletedFalseOrderByCreatedAtDesc(invoice.getId());
            totalPaid = payments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return mapToPaymentResponse(payment, invoice, totalPaid);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment, Invoice invoice, BigDecimal totalPaid) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentNumber(payment.getPaymentNumber());
        response.setInvoiceId(payment.getInvoiceId());
        response.setCustomerId(payment.getCustomerId());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaymentMode(payment.getPaymentMode());
        response.setAmount(payment.getAmount());
        response.setReferenceNumber(payment.getReferenceNumber());
        response.setNotes(payment.getNotes());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        // Resolve Invoice info
        if (invoice != null) {
            response.setInvoiceNumber(invoice.getInvoiceNumber());
            response.setInvoiceTotal(invoice.getGrandTotal());
            response.setInvoiceStatus(invoice.getStatus());

            BigDecimal grandTotal = invoice.getGrandTotal() != null ? invoice.getGrandTotal() : BigDecimal.ZERO;
            BigDecimal remaining = grandTotal.subtract(totalPaid != null ? totalPaid : BigDecimal.ZERO);
            response.setRemainingBalance(remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO);
        }

        // Resolve Customer name
        if (payment.getCustomerId() != null) {
            String customerName = resolveCustomerName(payment.getCustomerId());
            response.setCustomerName(customerName);
        }

        return response;
    }

    private String resolveCustomerName(UUID customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null) {
            if (customer.getDisplayName() != null && !customer.getDisplayName().isEmpty()) {
                return customer.getDisplayName();
            }
            if (customer.getCompanyName() != null && !customer.getCompanyName().isEmpty()) {
                return customer.getCompanyName();
            }
            String first = customer.getFirstName() != null ? customer.getFirstName() : "";
            String last = customer.getLastName() != null ? customer.getLastName() : "";
            String name = (first + " " + last).trim();
            if (!name.isEmpty()) {
                return name;
            }
        }

        User user = userRepository.findById(customerId).orElse(null);
        if (user != null) {
            String first = user.getFirstName() != null ? user.getFirstName() : "";
            String last = user.getLastName() != null ? user.getLastName() : "";
            String name = (first + " " + last).trim();
            if (!name.isEmpty()) {
                return name;
            }
            if (user.getEmail() != null) {
                return user.getEmail();
            }
        }

        return "Customer";
    }
}
