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

import com.example.registeration.entity.ProformaInvoice;

@Repository
public interface ProformaInvoiceRepository
        extends JpaRepository<ProformaInvoice, UUID>,
                JpaSpecificationExecutor<ProformaInvoice> {

    @Query("SELECT p FROM ProformaInvoice p WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(p.proformaInvoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "p.customerId IN (SELECT u.id FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))) AND " +
           "(:status IS NULL OR :status = '' OR LOWER(p.status) = LOWER(:status)) AND " +
           "(:startDate IS NULL OR p.proformaInvoiceDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.proformaInvoiceDate <= :endDate) AND " +
           "p.isDeleted = false")
    Page<ProformaInvoice> findAllProformaInvoices(
            @Param("search") String search,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
