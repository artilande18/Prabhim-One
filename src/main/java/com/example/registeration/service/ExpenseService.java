package com.example.registeration.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.registeration.dto.ExpenseRequest;
import com.example.registeration.dto.ExpenseResponse;
import com.example.registeration.entity.Expense;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.ExpenseRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listExpenses() {
        return expenseRepository.findAll().stream()
                .filter(e -> !e.getIsDeleted())
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request, UUID userId) {
        long count = expenseRepository.count();
        String expenseNumber = String.format("EXP-%03d", count + 1);

        Expense expense = new Expense();
        expense.setExpenseNumber(expenseNumber);
        expense.setDate(request.getDate());
        expense.setCategory(request.getCategory());
        expense.setReference(request.getReference());
        expense.setPaymentMode(request.getPaymentMode());
        expense.setAmount(request.getAmount());
        expense.setNotes(request.getNotes());
        
        expense.setCreatedBy(userId);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());
        expense.setIsDeleted(false);

        Expense saved = expenseRepository.save(expense);
        return mapToExpenseResponse(saved);
    }

    @Transactional
    public void deleteExpense(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .filter(e -> !e.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

        expense.setIsDeleted(true);
        expense.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense);
    }

    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setExpenseNumber(expense.getExpenseNumber());
        response.setDate(expense.getDate());
        response.setCategory(expense.getCategory());
        response.setReference(expense.getReference());
        response.setPaymentMode(expense.getPaymentMode());
        response.setAmount(expense.getAmount());
        response.setNotes(expense.getNotes());
        response.setCreatedAt(expense.getCreatedAt());
        response.setUpdatedAt(expense.getUpdatedAt());
        return response;
    }
}
