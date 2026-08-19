package com.example.registeration.repository;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.registeration.entity.Invoice;

@Repository
public interface InvoiceRepository
        extends JpaRepository<Invoice, UUID>,
                JpaSpecificationExecutor<Invoice> {

    @Query("SELECT i FROM Invoice i WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "i.customerId IN (SELECT u.id FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))) AND " +
           "(:status IS NULL OR :status = '' OR LOWER(i.status) = LOWER(:status)) AND " +
           "(:startDate IS NULL OR i.invoiceDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.invoiceDate <= :endDate) AND " +
           "i.isDeleted = false")
    Page<Invoice> findAllInvoices(
            @Param("search") String search,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
