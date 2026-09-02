package com.example.registeration.service;

import java.time.LocalDate;
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

import com.example.registeration.dto.BillResponse;
import com.example.registeration.dto.PurchaseOrderRequest;
import com.example.registeration.dto.PurchaseOrderResponse;
import com.example.registeration.entity.Bill;
import com.example.registeration.entity.PurchaseOrder;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.BillRepository;
import com.example.registeration.repository.PurchaseOrderRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final BillRepository billRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository, BillRepository billRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.billRepository = billRepository;
    }

    private PurchaseOrder findPurchaseOrder(String idOrNumber) {
        try {
            UUID uuid = UUID.fromString(idOrNumber);
            PurchaseOrder po = purchaseOrderRepository.findById(uuid)
                    .filter(p -> !p.getIsDeleted())
                    .orElse(null);
            if (po != null) return po;
        } catch (IllegalArgumentException e) {
            // Not a UUID, fallback to lookup by poNumber
        }

        return purchaseOrderRepository.findAll().stream()
                .filter(p -> !p.getIsDeleted() && idOrNumber.equalsIgnoreCase(p.getPoNumber()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID/Number: " + idOrNumber));
    }

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request, UUID userId) {
        long count = purchaseOrderRepository.count();
        String poNumber = String.format("PO-%03d", count + 1);

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(poNumber);
        updatePOFields(po, request);
        po.setCreatedBy(userId);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return mapToPOResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> listPurchaseOrders(
            String search,
            String status,
            int page,
            int pageSize) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Specification<PurchaseOrder> spec = (root, query, cb) -> {
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

        Page<PurchaseOrder> pageResult = purchaseOrderRepository.findAll(spec, pageable);
        List<PurchaseOrderResponse> content = pageResult.getContent().stream()
                .map(this::mapToPOResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderDetails(String idOrNumber) {
        PurchaseOrder po = findPurchaseOrder(idOrNumber);
        return mapToPOResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse updatePurchaseOrder(String idOrNumber, PurchaseOrderRequest request, UUID userId) {
        PurchaseOrder po = findPurchaseOrder(idOrNumber);
        updatePOFields(po, request);
        po.setUpdatedAt(LocalDateTime.now());

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return mapToPOResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(String idOrNumber, UUID userId) {
        PurchaseOrder po = findPurchaseOrder(idOrNumber);
        po.setStatus("Cancelled");
        po.setCancelledAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return mapToPOResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse restoreCancelledPO(String idOrNumber, UUID userId) {
        // Since findPurchaseOrder excludes soft-deleted (isDeleted=true) but this record is Cancelled (status="Cancelled"), we can still find it
        PurchaseOrder po = findPurchaseOrder(idOrNumber);
        if (!"Cancelled".equalsIgnoreCase(po.getStatus())) {
            throw new IllegalArgumentException("Purchase order is not in Cancelled status.");
        }
        po.setStatus("Ordered");
        po.setCancelledAt(null);
        po.setUpdatedAt(LocalDateTime.now());

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return mapToPOResponse(saved);
    }

    @Transactional
    public void permanentlyDeletePO(String idOrNumber, UUID userId) {
        PurchaseOrder po = findPurchaseOrder(idOrNumber);
        if (!"Cancelled".equalsIgnoreCase(po.getStatus())) {
            throw new IllegalArgumentException("Only cancelled purchase orders can be permanently deleted.");
        }
        purchaseOrderRepository.delete(po);
    }

    @Transactional
    public BillResponse convertToBill(String idOrNumber, String billNumber, UUID userId) {
        PurchaseOrder po = findPurchaseOrder(idOrNumber);
        
        // Update PO status to Received
        po.setStatus("Received");
        po.setUpdatedAt(LocalDateTime.now());
        purchaseOrderRepository.save(po);

        // Create new Bill
        Bill bill = new Bill();
        bill.setBillNumber(billNumber);
        bill.setOriginalPoId(po.getPoNumber());
        bill.setDate(po.getDate());
        // Default due date to 30 days after bill date
        bill.setDueDate(po.getDate() != null ? po.getDate().plusDays(30) : LocalDate.now().plusDays(30));
        bill.setVendor(po.getVendor());
        bill.setCategory(po.getCategory());
        bill.setStatus("Pending");
        bill.setAmount(po.getAmount());
        bill.setTax(po.getTax());
        bill.setTotal(po.getTotal());
        bill.setNotes(po.getNotes());
        bill.setConvertedAt(LocalDateTime.now());
        bill.setCreatedBy(userId);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());

        Bill savedBill = billRepository.save(bill);
        return mapToBillResponse(savedBill);
    }

    private void updatePOFields(PurchaseOrder po, PurchaseOrderRequest request) {
        if (request.getDate() != null) po.setDate(request.getDate());
        if (request.getVendor() != null) po.setVendor(request.getVendor());
        if (request.getCategory() != null) po.setCategory(request.getCategory());
        if (request.getReference() != null) po.setReference(request.getReference());
        if (request.getPaymentMode() != null) po.setPaymentMode(request.getPaymentMode());
        if (request.getStatus() != null) po.setStatus(request.getStatus());
        if (request.getAmount() != null) po.setAmount(request.getAmount());
        if (request.getGstRate() != null) po.setGstRate(request.getGstRate());
        if (request.getTax() != null) po.setTax(request.getTax());
        if (request.getTotal() != null) po.setTotal(request.getTotal());
        if (request.getNotes() != null) po.setNotes(request.getNotes());
    }

    private PurchaseOrderResponse mapToPOResponse(PurchaseOrder po) {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setDbId(po.getId());
        response.setPoNumber(po.getPoNumber());
        response.setDate(po.getDate());
        response.setVendor(po.getVendor());
        response.setCategory(po.getCategory());
        response.setReference(po.getReference());
        response.setPaymentMode(po.getPaymentMode());
        response.setStatus(po.getStatus());
        response.setAmount(po.getAmount());
        response.setGstRate(po.getGstRate());
        response.setTax(po.getTax());
        response.setTotal(po.getTotal());
        response.setNotes(po.getNotes());
        response.setCancelledAt(po.getCancelledAt());
        response.setCreatedAt(po.getCreatedAt());
        return response;
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
