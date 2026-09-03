package com.example.registeration.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.registeration.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByIsDeletedFalseOrderByCreatedAtDesc();
    List<Payment> findByIsDeletedFalse();
    List<Payment> findByIsDeletedFalseAndPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    List<Payment> findByInvoiceIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID invoiceId);
    List<Payment> findByCustomerIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID customerId);
    Optional<Payment> findByIdAndIsDeletedFalse(UUID id);
}

