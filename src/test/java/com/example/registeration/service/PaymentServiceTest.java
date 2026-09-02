package com.example.registeration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.registeration.dto.PaymentRequest;
import com.example.registeration.dto.PaymentResponse;
import com.example.registeration.entity.Invoice;
import com.example.registeration.entity.Payment;
import com.example.registeration.entity.User;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.CustomerRepository;
import com.example.registeration.repository.InvoiceRepository;
import com.example.registeration.repository.PaymentRepository;
import com.example.registeration.repository.UserRepository;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private InvoiceRepository invoiceRepository;
    private UserRepository userRepository;
    private CustomerRepository customerRepository;

    private PaymentService paymentService;

    private UUID userId;
    private UUID customerId;
    private UUID invoiceId;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        invoiceRepository = mock(InvoiceRepository.class);
        userRepository = mock(UserRepository.class);
        customerRepository = mock(CustomerRepository.class);

        paymentService = new PaymentService(
                paymentRepository,
                invoiceRepository,
                userRepository,
                customerRepository
        );

        userId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();

        invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setInvoiceNumber("INV-000001");
        invoice.setCustomerId(customerId);
        invoice.setGrandTotal(BigDecimal.valueOf(1000.00));
        invoice.setStatus("sent");
        invoice.setDeleted(false);
    }

    @Test
    void recordPayment_PartialPayment_ShouldUpdateInvoiceToPartiallyPaid() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceIdAndIsDeletedFalseOrderByCreatedAtDesc(invoiceId)).thenReturn(new ArrayList<>());
        when(paymentRepository.count()).thenReturn(0L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(BigDecimal.valueOf(400.00));
        request.setPaymentMode("UPI");
        request.setPaymentDate(LocalDate.now());

        PaymentResponse response = paymentService.recordPayment(request, userId);

        assertNotNull(response);
        assertEquals("PAY-000001", response.getPaymentNumber());
        assertEquals(BigDecimal.valueOf(400.00), response.getAmount());
        assertEquals("partially_paid", invoice.getStatus());
        verify(invoiceRepository, times(1)).save(invoice);
    }

    @Test
    void recordPayment_FullPayment_ShouldUpdateInvoiceToPaid() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceIdAndIsDeletedFalseOrderByCreatedAtDesc(invoiceId)).thenReturn(new ArrayList<>());
        when(paymentRepository.count()).thenReturn(0L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(BigDecimal.valueOf(1000.00));
        request.setPaymentMode("Bank Transfer");

        PaymentResponse response = paymentService.recordPayment(request, userId);

        assertNotNull(response);
        assertEquals("paid", invoice.getStatus());
        verify(invoiceRepository, times(1)).save(invoice);
    }

    @Test
    void recordPayment_ExceedingAmount_ShouldThrowException() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceIdAndIsDeletedFalseOrderByCreatedAtDesc(invoiceId)).thenReturn(new ArrayList<>());

        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(BigDecimal.valueOf(1500.00)); // Exceeds 1000

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.recordPayment(request, userId)
        );

        assertTrue(ex.getMessage().contains("exceeds invoice remaining balance"));
    }

    @Test
    void recordPayment_AlreadyFullyPaid_ShouldThrowException() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        Payment existingPayment = new Payment();
        existingPayment.setAmount(BigDecimal.valueOf(1000.00));
        when(paymentRepository.findByInvoiceIdAndIsDeletedFalseOrderByCreatedAtDesc(invoiceId))
                .thenReturn(List.of(existingPayment));

        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(BigDecimal.valueOf(100.00));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.recordPayment(request, userId)
        );

        assertTrue(ex.getMessage().contains("already fully paid"));
    }

    @Test
    void deletePayment_ShouldSoftDeleteAndReconcileInvoice() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setInvoiceId(invoiceId);
        payment.setAmount(BigDecimal.valueOf(1000.00));
        payment.setIsDeleted(false);

        invoice.setStatus("paid");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(paymentRepository.findByIdAndIsDeletedFalse(paymentId)).thenReturn(Optional.of(payment));
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceIdAndIsDeletedFalseOrderByCreatedAtDesc(invoiceId)).thenReturn(new ArrayList<>());

        paymentService.deletePayment(paymentId, userId);

        assertTrue(payment.getIsDeleted());
        assertNotNull(payment.getDeletedAt());
        assertEquals("sent", invoice.getStatus());
        verify(paymentRepository, times(1)).save(payment);
        verify(invoiceRepository, times(1)).save(invoice);
    }
}
