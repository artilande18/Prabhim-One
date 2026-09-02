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

import com.example.registeration.dto.CreateProformaInvoiceRequest;
import com.example.registeration.dto.ProformaInvoiceItemRequest;
import com.example.registeration.dto.ProformaInvoiceResponse;
import com.example.registeration.entity.ProformaInvoice;
import com.example.registeration.entity.ProformaInvoiceItem;
import com.example.registeration.entity.User;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.ProformaInvoiceItemRepository;
import com.example.registeration.repository.ProformaInvoiceRepository;
import com.example.registeration.repository.UserRepository;

@Service
public class ProformaInvoiceService {

    private final ProformaInvoiceRepository proformaInvoiceRepository;
    private final ProformaInvoiceItemRepository proformaInvoiceItemRepository;
    private final UserRepository userRepository;

    public ProformaInvoiceService(
            ProformaInvoiceRepository proformaInvoiceRepository,
            ProformaInvoiceItemRepository proformaInvoiceItemRepository,
            UserRepository userRepository) {

        this.proformaInvoiceRepository = proformaInvoiceRepository;
        this.proformaInvoiceItemRepository = proformaInvoiceItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProformaInvoiceResponse createProformaInvoice(CreateProformaInvoiceRequest request) {
        User user = userRepository.findById(request.getCustomer())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomer()));

        long pfiCount = proformaInvoiceRepository.count();
        String pfiNumber = String.format("PFI-%06d", pfiCount + 1);

        ProformaInvoice pfi = new ProformaInvoice();
        pfi.setProformaInvoiceNumber(pfiNumber);
        pfi.setCustomerId(request.getCustomer());
        pfi.setProformaInvoiceDate(request.getProformaInvoiceDate());
        pfi.setDueDate(request.getDueDate());
        pfi.setStatus("draft");
        pfi.setCurrency(request.getCurrency());
        pfi.setNotes(request.getNotes());
        pfi.setTermsAndConditions(request.getTermsAndConditions());
        pfi.setDeleted(false);
        pfi.setCreatedAt(LocalDateTime.now());
        pfi.setUpdatedAt(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        List<ProformaInvoiceItem> pfiItems = new ArrayList<>();

        if (request.getItems() != null) {
            for (ProformaInvoiceItemRequest itemReq : request.getItems()) {
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

                ProformaInvoiceItem item = new ProformaInvoiceItem();
                item.setProductId(itemReq.getProduct());
                item.setDescription("");
                item.setQuantity(quantity);
                item.setUnitPrice(unitPrice);
                item.setDiscount(discount);
                item.setTax(taxPercent);
                item.setLineTotal(lineTotal);
                pfiItems.add(item);
            }
        }

        pfi.setSubtotal(subtotal);
        pfi.setDiscountTotal(discountTotal);
        pfi.setTaxTotal(taxTotal);
        pfi.setGrandTotal(grandTotal);

        ProformaInvoice savedPfi = proformaInvoiceRepository.save(pfi);

        for (ProformaInvoiceItem item : pfiItems) {
            item.setProformaInvoiceId(savedPfi.getId());
            proformaInvoiceItemRepository.save(item);
        }

        return mapToProformaInvoiceResponse(savedPfi, user);
    }

    public ProformaInvoiceResponse getProformaInvoice(UUID id) {
        ProformaInvoice pfi = proformaInvoiceRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Proforma Invoice not found with id: " + id));

        User user = userRepository.findById(pfi.getCustomerId()).orElse(null);
        return mapToProformaInvoiceResponse(pfi, user);
    }

    public Page<ProformaInvoiceResponse> getProformaInvoices(
            String search,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int pageSize) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<ProformaInvoice> pfis = proformaInvoiceRepository.findAllProformaInvoices(search, status, startDate, endDate, pageable);

        List<ProformaInvoiceResponse> content = pfis.getContent().stream()
                .map(pfi -> {
                    User user = userRepository.findById(pfi.getCustomerId()).orElse(null);
                    return mapToProformaInvoiceResponse(pfi, user);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, pfis.getTotalElements());
    }

    public Map<String, Object> getProformaInvoiceDetails(UUID id) {
        ProformaInvoice pfi = proformaInvoiceRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Proforma Invoice not found with id: " + id));

        User user = userRepository.findById(pfi.getCustomerId()).orElse(null);
        List<ProformaInvoiceItem> items = proformaInvoiceItemRepository.findByProformaInvoiceId(id);

        Map<String, Object> customerMap = new LinkedHashMap<>();
        if (user != null) {
            customerMap.put("id", user.getId());
            customerMap.put("display_name", user.getFirstName() + " " + user.getLastName());
            customerMap.put("company_name", user.getFirstName() + " Inc");
            customerMap.put("email", user.getEmail());
        } else {
            customerMap.put("id", pfi.getCustomerId());
            customerMap.put("display_name", "Unknown Customer");
            customerMap.put("company_name", "Unknown Inc");
            customerMap.put("email", "unknown@example.com");
        }

        List<Map<String, Object>> itemsList = items.stream().map(item -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("id", item.getId());

            Map<String, Object> productMap = new LinkedHashMap<>();
            productMap.put("id", item.getProductId());
            productMap.put("name", "Logitech MX Master 3S");
            productMap.put("sku", "MX-MASTER-3S");
            productMap.put("product_type", "goods");
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
        data.put("id", pfi.getId());
        data.put("customer", customerMap);
        data.put("items", itemsList);
        data.put("created_by", user != null ? user.getEmail() : "unknown@example.com");
        data.put("organization", "org-uuid-here");
        data.put("proforma_invoice_number", pfi.getProformaInvoiceNumber());
        data.put("proforma_invoice_date", pfi.getProformaInvoiceDate());
        data.put("due_date", pfi.getDueDate());
        data.put("status", pfi.getStatus());
        data.put("currency", pfi.getCurrency());
        data.put("notes", pfi.getNotes() != null ? pfi.getNotes() : "");
        data.put("terms_and_conditions", pfi.getTermsAndConditions() != null ? pfi.getTermsAndConditions() : "");
        data.put("subtotal", String.format("%.2f", pfi.getSubtotal()));
        data.put("discount_total", String.format("%.2f", pfi.getDiscountTotal()));
        data.put("tax_total", String.format("%.2f", pfi.getTaxTotal()));
        data.put("grand_total", String.format("%.2f", pfi.getGrandTotal()));
        data.put("created_at", pfi.getCreatedAt());
        data.put("updated_at", pfi.getUpdatedAt());
        data.put("is_deleted", pfi.isDeleted());
        data.put("deleted_at", pfi.getDeletedAt());

        return data;
    }

    private ProformaInvoiceResponse mapToProformaInvoiceResponse(ProformaInvoice pfi, User user) {
        ProformaInvoiceResponse response = new ProformaInvoiceResponse();
        response.setId(pfi.getId());
        response.setProformaInvoiceNumber(pfi.getProformaInvoiceNumber());
        response.setCustomer(pfi.getCustomerId());
        if (user != null) {
            response.setCustomerName(user.getFirstName() + " " + user.getLastName());
            response.setCreatedByEmail(user.getEmail());
        } else {
            response.setCustomerName("Unknown Customer");
            response.setCreatedByEmail("unknown@example.com");
        }
        response.setProformaInvoiceDate(pfi.getProformaInvoiceDate());
        response.setDueDate(pfi.getDueDate());
        response.setStatus(pfi.getStatus());
        response.setCurrency(pfi.getCurrency());
        response.setSubtotal(pfi.getSubtotal());
        response.setDiscountTotal(pfi.getDiscountTotal());
        response.setTaxTotal(pfi.getTaxTotal());
        response.setGrandTotal(pfi.getGrandTotal());
        response.setCreatedAt(pfi.getCreatedAt());
        response.setUpdatedAt(pfi.getUpdatedAt());
        response.setDeleted(pfi.isDeleted());
        response.setDeletedAt(pfi.getDeletedAt());
        return response;
    }
}
