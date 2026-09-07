package com.example.registeration.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.example.registeration.entity.Purchase;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, UUID>, JpaSpecificationExecutor<Purchase> {
    long count();
    List<Purchase> findByIsDeletedFalse();
    List<Purchase> findByIsDeletedFalseAndDateBetween(LocalDate startDate, LocalDate endDate);
}

