package com.example.registeration.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.registeration.entity.CreditNote;

@Repository
public interface CreditNoteRepository extends JpaRepository<CreditNote, UUID> {

    @Query("SELECT cn FROM CreditNote cn WHERE cn.isDeleted = false "
            + "AND (:status IS NULL OR cn.status = :status) "
            + "AND (:startDate IS NULL OR cn.creditNoteDate >= :startDate) "
            + "AND (:endDate IS NULL OR cn.creditNoteDate <= :endDate) "
            + "AND (:search IS NULL OR LOWER(cn.creditNoteNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<CreditNote> findWithFilters(
            @Param("search") String search,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    Optional<CreditNote> findByIdAndIsDeletedFalse(UUID id);
}
