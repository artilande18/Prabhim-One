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

import com.example.registeration.entity.Estimate;

@Repository
public interface EstimateRepository
        extends JpaRepository<Estimate, UUID>,
                JpaSpecificationExecutor<Estimate> {

    @Query("SELECT e FROM Estimate e WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(e.estimateNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "e.customerId IN (SELECT u.id FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))) AND " +
           "(:status IS NULL OR :status = '' OR LOWER(e.status) = LOWER(:status)) AND " +
           "(:startDate IS NULL OR e.estimateDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.estimateDate <= :endDate) AND " +
           "e.isDeleted = false")
    Page<Estimate> findAllEstimates(
            @Param("search") String search,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
