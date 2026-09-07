package com.example.registeration.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.registeration.entity.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByIsDeletedFalse();
    List<Expense> findByIsDeletedFalseAndDateBetween(LocalDate startDate, LocalDate endDate);
}

