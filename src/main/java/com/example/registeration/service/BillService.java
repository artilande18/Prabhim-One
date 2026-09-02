package com.example.registeration.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.registeration.dto.BillRequest;
import com.example.registeration.dto.BillResponse;
import com.example.registeration.entity.Bill;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.BillRepository;

@Service
public class BillService {

    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    private Bill findBill(String idOrNumber) {
        try {
            UUID uuid = UUID.fromString(idOrNumber);
            Bill bill = billRepository.findById(uuid)
                    .filter(b -> !b.getIsDeleted())
                    .orElse(null);
            if (bill != null) return bill;
        } catch (IllegalArgumentException e) {
            // Not a UUID, fallback to lookup by billNumber
        }

        return billRepository.findAll().stream()
                .filter(b -> !b.getIsDeleted() && idOrNumber.equalsIgnoreCase(b.getBillNumber()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID/Number: " + idOrNumber));
    }

    @Transactional(readOnly = true)
    public Page<BillResponse> listBills(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Bill> billsPage = billRepository.findAll(pageable);

        List<BillResponse> responses = billsPage.getContent().stream()
                .filter(b -> !b.getIsDeleted())
                .map(this::mapToBillResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, billsPage.getTotalElements());
    }

    @Transactional
    public BillResponse updateBill(String idOrNumber, BillRequest request, UUID userId) {
        Bill bill = findBill(idOrNumber);
        if (request.getStatus() != null) bill.setStatus(request.getStatus());
        if (request.getDueDate() != null) bill.setDueDate(request.getDueDate());
        if (request.getNotes() != null) bill.setNotes(request.getNotes());
        bill.setUpdatedAt(LocalDateTime.now());

        Bill saved = billRepository.save(bill);
        return mapToBillResponse(saved);
    }

    private BillResponse mapToBillResponse(Bill bill) {
        BillResponse response = new BillResponse();
        response.setDbId(bill.getId());
        response.setBillNumber(bill.getBillNumber());
        response.setOriginalPoId(bill.getOriginalPoId());
        response.setDate(bill.getDate());
        response.setDueDate(bill.getDueDate());
        response.setVendor(bill.getVendor());
        response.setCategory(bill.getCategory());
        response.setStatus(bill.getStatus());
        response.setAmount(bill.getAmount());
        response.setTax(bill.getTax());
        response.setTotal(bill.getTotal());
        response.setNotes(bill.getNotes());
        response.setConvertedAt(bill.getConvertedAt());
        return response;
    }
}
