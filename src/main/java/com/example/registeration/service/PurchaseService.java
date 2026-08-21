package com.example.registeration.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.registeration.dto.PurchaseRequest;
import com.example.registeration.dto.PurchaseResponse;
import com.example.registeration.entity.Purchase;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.PurchaseRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    public PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    private Purchase findPurchase(String idOrNumber) {
        try {
            UUID uuid = UUID.fromString(idOrNumber);
            Purchase purchase = purchaseRepository.findById(uuid)
                    .filter(p -> !p.getIsDeleted())
                    .orElse(null);
            if (purchase != null) return purchase;
        } catch (IllegalArgumentException e) {
            // Not a UUID, fallback to lookup by purchaseNumber
        }

        return purchaseRepository.findAll().stream()
                .filter(p -> !p.getIsDeleted() && idOrNumber.equalsIgnoreCase(p.getPurchaseNumber()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Purchase record not found with ID/Number: " + idOrNumber));
    }

    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest request, UUID userId) {
        long count = purchaseRepository.count();
        String purchaseNumber = String.format("PUR-%03d", count + 1);

        Purchase purchase = new Purchase();
        purchase.setPurchaseNumber(purchaseNumber);
        updatePurchaseFields(purchase, request);
        purchase.setCreatedBy(userId);
        purchase.setCreatedAt(LocalDateTime.now());
        purchase.setUpdatedAt(LocalDateTime.now());

        Purchase saved = purchaseRepository.save(purchase);
        return mapToPurchaseResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseResponse> listPurchases(
            String search,
            String status,
            int page,
            int pageSize) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Specification<Purchase> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("vendor")), pattern),
                        cb.like(cb.lower(root.get("category")), pattern),
                        cb.like(cb.lower(root.get("reference")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Purchase> recordsPage = purchaseRepository.findAll(spec, pageable);
        List<PurchaseResponse> responses = recordsPage.getContent().stream()
                .map(this::mapToPurchaseResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, recordsPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseDetails(String idOrNumber) {
        Purchase purchase = findPurchase(idOrNumber);
        return mapToPurchaseResponse(purchase);
    }

    @Transactional
    public PurchaseResponse updatePurchase(String idOrNumber, PurchaseRequest request, UUID userId) {
        Purchase purchase = findPurchase(idOrNumber);
        updatePurchaseFields(purchase, request);
        purchase.setUpdatedAt(LocalDateTime.now());

        Purchase saved = purchaseRepository.save(purchase);
        return mapToPurchaseResponse(saved);
    }

    @Transactional
    public PurchaseResponse deletePurchase(String idOrNumber, UUID userId) {
        Purchase purchase = findPurchase(idOrNumber);
        purchase.setStatus("Cancelled");
        purchase.setCancelledAt(LocalDateTime.now());
        purchase.setUpdatedAt(LocalDateTime.now());

        Purchase saved = purchaseRepository.save(purchase);
        return mapToPurchaseResponse(saved);
    }

    private void updatePurchaseFields(Purchase purchase, PurchaseRequest request) {
        if (request.getDate() != null) purchase.setDate(request.getDate());
        if (request.getVendor() != null) purchase.setVendor(request.getVendor());
        if (request.getCategory() != null) purchase.setCategory(request.getCategory());
        if (request.getReference() != null) purchase.setReference(request.getReference());
        if (request.getPaymentMode() != null) purchase.setPaymentMode(request.getPaymentMode());
        if (request.getStatus() != null) purchase.setStatus(request.getStatus());
        if (request.getAmount() != null) purchase.setAmount(request.getAmount());
        if (request.getGstRate() != null) purchase.setGstRate(request.getGstRate());
        if (request.getTax() != null) purchase.setTax(request.getTax());
        if (request.getTotal() != null) purchase.setTotal(request.getTotal());
        if (request.getNotes() != null) purchase.setNotes(request.getNotes());
    }

    private PurchaseResponse mapToPurchaseResponse(Purchase purchase) {
        PurchaseResponse response = new PurchaseResponse();
        response.setDbId(purchase.getId());
        response.setPurchaseNumber(purchase.getPurchaseNumber());
        response.setDate(purchase.getDate());
        response.setVendor(purchase.getVendor());
        response.setCategory(purchase.getCategory());
        response.setReference(purchase.getReference());
        response.setPaymentMode(purchase.getPaymentMode());
        response.setStatus(purchase.getStatus());
        response.setAmount(purchase.getAmount());
        response.setGstRate(purchase.getGstRate());
        response.setTax(purchase.getTax());
        response.setTotal(purchase.getTotal());
        response.setNotes(purchase.getNotes());
        response.setCreatedAt(purchase.getCreatedAt());
        return response;
    }
}
