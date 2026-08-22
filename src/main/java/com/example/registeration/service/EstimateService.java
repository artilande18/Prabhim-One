package com.example.registeration.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.registeration.dto.CreateEstimateRequest;
import com.example.registeration.dto.EstimateItemRequest;
import com.example.registeration.dto.EstimateResponse;
import com.example.registeration.dto.UpdateEstimateRequest;
import com.example.registeration.entity.Estimate;
import com.example.registeration.entity.EstimateItem;
import com.example.registeration.entity.User;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.EstimateItemRepository;
import com.example.registeration.repository.EstimateRepository;
import com.example.registeration.repository.UserRepository;

@Service
public class EstimateService {

    private final EstimateRepository estimateRepository;
    private final EstimateItemRepository estimateItemRepository;
    private final UserRepository userRepository;

    public EstimateService(
            EstimateRepository estimateRepository,
            EstimateItemRepository estimateItemRepository,
            UserRepository userRepository) {

        this.estimateRepository = estimateRepository;
        this.estimateItemRepository = estimateItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EstimateResponse createEstimate(CreateEstimateRequest request) {
        User user = userRepository.findById(request.getCustomer())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomer()));

        long estimateCount = estimateRepository.count();
        String estimateNumber = String.format("EST-%06d", estimateCount + 1);

        Estimate estimate = new Estimate();
        estimate.setEstimateNumber(estimateNumber);
        estimate.setCustomerId(request.getCustomer());
        estimate.setEstimateDate(request.getEstimateDate());
        estimate.setExpiryDate(request.getExpiryDate());
        estimate.setStatus("draft");
        estimate.setCurrency(request.getCurrency());
        estimate.setNotes(request.getNotes());
        estimate.setTermsAndConditions(request.getTermsAndConditions());
        estimate.setDeleted(false);
        estimate.setCreatedAt(LocalDateTime.now());
        estimate.setUpdatedAt(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        List<EstimateItem> estimateItems = new ArrayList<>();

        if (request.getItems() != null) {
            for (EstimateItemRequest itemReq : request.getItems()) {
                BigDecimal unitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.valueOf(1000.00);
                BigDecimal taxPercent = itemReq.getTax() != null ? itemReq.getTax() : BigDecimal.valueOf(18.00);
                BigDecimal discount = itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO;
                BigDecimal quantity = itemReq.getQuantity() != null ? itemReq.getQuantity() : BigDecimal.ONE;

                BigDecimal itemSubtotal = unitPrice.multiply(quantity);
                BigDecimal taxableAmount = itemSubtotal.subtract(discount);
                if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
                    taxableAmount = BigDecimal.ZERO;
                }
                BigDecimal taxVal = taxableAmount.multiply(taxPercent.divide(BigDecimal.valueOf(100)));
                BigDecimal lineTotal = taxableAmount.add(taxVal);

                subtotal = subtotal.add(itemSubtotal);
                discountTotal = discountTotal.add(discount);
                taxTotal = taxTotal.add(taxVal);
                grandTotal = grandTotal.add(lineTotal);

                EstimateItem item = new EstimateItem();
                item.setProductId(itemReq.getProduct());
                item.setDescription("");
                item.setQuantity(quantity);
                item.setUnitPrice(unitPrice);
                item.setDiscount(discount);
                item.setTax(taxPercent);
                item.setLineTotal(lineTotal);
                estimateItems.add(item);
            }
        }

        estimate.setSubtotal(subtotal);
        estimate.setDiscountTotal(discountTotal);
        estimate.setTaxTotal(taxTotal);
        estimate.setGrandTotal(grandTotal);

        Estimate savedEstimate = estimateRepository.save(estimate);

        for (EstimateItem item : estimateItems) {
            item.setEstimateId(savedEstimate.getId());
            estimateItemRepository.save(item);
        }

        return mapToEstimateResponse(savedEstimate, user);
    }

    public EstimateResponse getEstimate(UUID id) {
        Estimate estimate = estimateRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + id));

        User user = userRepository.findById(estimate.getCustomerId()).orElse(null);
        return mapToEstimateResponse(estimate, user);
    }

    public Page<EstimateResponse> getEstimates(
            String search,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int pageSize) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Estimate> estimates = estimateRepository.findAllEstimates(search, status, startDate, endDate, pageable);

        List<EstimateResponse> content = estimates.getContent().stream()
                .map(estimate -> {
                    User user = userRepository.findById(estimate.getCustomerId()).orElse(null);
                    return mapToEstimateResponse(estimate, user);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, estimates.getTotalElements());
    }

    @Transactional
    public EstimateResponse updateEstimate(UUID id, UpdateEstimateRequest request) {
        Estimate estimate = estimateRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + id));

        if (!"draft".equalsIgnoreCase(estimate.getStatus())) {
            throw new IllegalArgumentException("Only draft estimates can be updated");
        }

        if (request.getExpiryDate() != null) {
            estimate.setExpiryDate(request.getExpiryDate());
        }
        if (request.getNotes() != null) {
            estimate.setNotes(request.getNotes());
        }
        if (request.getTermsAndConditions() != null) {
            estimate.setTermsAndConditions(request.getTermsAndConditions());
        }
        if (request.getStatus() != null) {
            estimate.setStatus(request.getStatus());
        }

        estimate.setUpdatedAt(LocalDateTime.now());
        Estimate updatedEstimate = estimateRepository.save(estimate);

        User user = userRepository.findById(updatedEstimate.getCustomerId()).orElse(null);
        return mapToEstimateResponse(updatedEstimate, user);
    }

    @Transactional
    public void deleteEstimate(UUID id) {
        Estimate estimate = estimateRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + id));

        if (!"draft".equalsIgnoreCase(estimate.getStatus())) {
            throw new IllegalArgumentException("Only draft estimates can be deleted");
        }

        estimate.setDeleted(true);
        estimate.setDeletedAt(LocalDateTime.now());
        estimateRepository.save(estimate);
    }

    public Map<String, Object> getEstimateDetails(UUID id) {
        Estimate estimate = estimateRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + id));

        User user = userRepository.findById(estimate.getCustomerId()).orElse(null);
        List<EstimateItem> items = estimateItemRepository.findByEstimateId(id);

        Map<String, Object> customerMap = new LinkedHashMap<>();
        if (user != null) {
            customerMap.put("id", user.getId());
            customerMap.put("display_name", user.getFirstName() + " " + user.getLastName());
            customerMap.put("company_name", user.getFirstName() + " Inc");
            customerMap.put("email", user.getEmail());
        } else {
            customerMap.put("id", estimate.getCustomerId());
            customerMap.put("display_name", "Unknown Customer");
            customerMap.put("company_name", "Unknown Inc");
            customerMap.put("email", "unknown@example.com");
        }

        List<Map<String, Object>> itemsList = items.stream().map(item -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("id", item.getId());

            Map<String, Object> productMap = new LinkedHashMap<>();
            productMap.put("id", item.getProductId());
            productMap.put("name", "Standard Service");
            productMap.put("sku", "SVC-STANDARD");
            productMap.put("product_type", "services");
            itemMap.put("product", productMap);

            itemMap.put("description", item.getDescription() != null ? item.getDescription() : "");
            itemMap.put("quantity", String.format("%.2f", item.getQuantity()));
            itemMap.put("unit_price", String.format("%.2f", item.getUnitPrice()));
            itemMap.put("discount", String.format("%.2f", item.getDiscount()));
            itemMap.put("tax", String.format("%.2f", item.getTax()));
            itemMap.put("line_total", String.format("%.2f", item.getLineTotal()));
            return itemMap;
        }).collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", estimate.getId());
        data.put("customer", customerMap);
        data.put("items", itemsList);
        data.put("created_by", user != null ? user.getEmail() : "unknown@example.com");
        data.put("organization", "org-uuid-here");
        data.put("estimate_number", estimate.getEstimateNumber());
        data.put("estimate_date", estimate.getEstimateDate());
        data.put("expiry_date", estimate.getExpiryDate());
        data.put("status", estimate.getStatus());
        data.put("currency", estimate.getCurrency());
        data.put("notes", estimate.getNotes() != null ? estimate.getNotes() : "");
        data.put("terms_and_conditions", estimate.getTermsAndConditions() != null ? estimate.getTermsAndConditions() : "");
        data.put("subtotal", String.format("%.2f", estimate.getSubtotal()));
        data.put("discount_total", String.format("%.2f", estimate.getDiscountTotal()));
        data.put("tax_total", String.format("%.2f", estimate.getTaxTotal()));
        data.put("grand_total", String.format("%.2f", estimate.getGrandTotal()));
        data.put("created_at", estimate.getCreatedAt());
        data.put("updated_at", estimate.getUpdatedAt());
        data.put("is_deleted", estimate.isDeleted());
        data.put("deleted_at", estimate.getDeletedAt());

        return data;
    }

    private EstimateResponse mapToEstimateResponse(Estimate estimate, User user) {
        EstimateResponse response = new EstimateResponse();
        response.setId(estimate.getId());
        response.setEstimateNumber(estimate.getEstimateNumber());
        response.setCustomer(estimate.getCustomerId());
        if (user != null) {
            response.setCustomerName(user.getFirstName() + " " + user.getLastName());
            response.setCreatedByEmail(user.getEmail());
        } else {
            response.setCustomerName("Unknown Customer");
            response.setCreatedByEmail("unknown@example.com");
        }
        response.setEstimateDate(estimate.getEstimateDate());
        response.setExpiryDate(estimate.getExpiryDate());
        response.setStatus(estimate.getStatus());
        response.setCurrency(estimate.getCurrency());
        response.setSubtotal(estimate.getSubtotal());
        response.setDiscountTotal(estimate.getDiscountTotal());
        response.setTaxTotal(estimate.getTaxTotal());
        response.setGrandTotal(estimate.getGrandTotal());
        response.setCreatedAt(estimate.getCreatedAt());
        response.setUpdatedAt(estimate.getUpdatedAt());
        response.setDeleted(estimate.isDeleted());
        response.setDeletedAt(estimate.getDeletedAt());
        return response;
    }
}
