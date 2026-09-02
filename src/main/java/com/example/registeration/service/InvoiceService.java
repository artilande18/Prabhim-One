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

import com.example.registeration.dto.CreateInvoiceRequest;
import com.example.registeration.dto.InvoiceItemRequest;
import com.example.registeration.dto.InvoiceResponse;
import com.example.registeration.dto.UpdateInvoiceRequest;
import com.example.registeration.entity.Invoice;
import com.example.registeration.entity.InvoiceItem;
import com.example.registeration.entity.User;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.InvoiceItemRepository;
import com.example.registeration.repository.InvoiceRepository;
import com.example.registeration.repository.UserRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final UserRepository userRepository;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceItemRepository invoiceItemRepository,
            UserRepository userRepository) {

        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        User user = userRepository.findById(request.getCustomer())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomer()));

        long invoiceCount = invoiceRepository.count();
        String invoiceNumber = String.format("INV-%06d", invoiceCount + 1);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomerId(request.getCustomer());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setStatus("draft");
        invoice.setCurrency(request.getCurrency());
        invoice.setNotes(request.getNotes());
        invoice.setTermsAndConditions(request.getTermsAndConditions());
        invoice.setDeleted(false);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        List<InvoiceItem> invoiceItems = new ArrayList<>();

        if (request.getItems() != null) {
            for (InvoiceItemRequest itemReq : request.getItems()) {
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

                InvoiceItem item = new InvoiceItem();
                item.setProductId(itemReq.getProduct());
                item.setDescription("");
                item.setQuantity(quantity);
                item.setUnitPrice(unitPrice);
                item.setDiscount(discount);
                item.setTax(taxPercent);
                item.setLineTotal(lineTotal);
                invoiceItems.add(item);
            }
        }

        invoice.setSubtotal(subtotal);
        invoice.setDiscountTotal(discountTotal);
        invoice.setTaxTotal(taxTotal);
        invoice.setGrandTotal(grandTotal);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        for (InvoiceItem item : invoiceItems) {
            item.setInvoiceId(savedInvoice.getId());
            invoiceItemRepository.save(item);
        }

        return mapToInvoiceResponse(savedInvoice, user);
    }

    public InvoiceResponse getInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        User user = userRepository.findById(invoice.getCustomerId()).orElse(null);
        return mapToInvoiceResponse(invoice, user);
    }

    public Page<InvoiceResponse> getInvoices(
            String search,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int pageSize) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Invoice> invoices = invoiceRepository.findAllInvoices(search, status, startDate, endDate, pageable);

        List<InvoiceResponse> content = invoices.getContent().stream()
                .map(invoice -> {
                    User user = userRepository.findById(invoice.getCustomerId()).orElse(null);
                    return mapToInvoiceResponse(invoice, user);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, invoices.getTotalElements());
    }

    @Transactional
    public InvoiceResponse updateInvoice(UUID id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        if (!"draft".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException("Only draft invoices can be updated");
        }

        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }
        if (request.getNotes() != null) {
            invoice.setNotes(request.getNotes());
        }
        if (request.getTermsAndConditions() != null) {
            invoice.setTermsAndConditions(request.getTermsAndConditions());
        }

        invoice.setUpdatedAt(LocalDateTime.now());
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        User user = userRepository.findById(updatedInvoice.getCustomerId()).orElse(null);
        return mapToInvoiceResponse(updatedInvoice, user);
    }

    @Transactional
    public void deleteInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        if (!"draft".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException("Only draft invoices can be deleted");
        }

        invoice.setDeleted(true);
        invoice.setDeletedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    @Transactional
    public InvoiceResponse markInvoiceSent(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        if (!"draft".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException("Only draft invoices can be marked as sent");
        }

        invoice.setStatus("sent");
        invoice.setUpdatedAt(LocalDateTime.now());
        Invoice saved = invoiceRepository.save(invoice);

        User user = userRepository.findById(saved.getCustomerId()).orElse(null);
        return mapToInvoiceResponse(saved, user);
    }

    public Map<String, Object> getInvoiceDetails(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        User user = userRepository.findById(invoice.getCustomerId()).orElse(null);
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);

        Map<String, Object> customerMap = new LinkedHashMap<>();
        if (user != null) {
            customerMap.put("id", user.getId());
            customerMap.put("display_name", user.getFirstName() + " " + user.getLastName());
            customerMap.put("company_name", user.getFirstName() + " Inc");
            customerMap.put("email", user.getEmail());
        } else {
            customerMap.put("id", invoice.getCustomerId());
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
        data.put("id", invoice.getId());
        data.put("customer", customerMap);
        data.put("items", itemsList);
        data.put("created_by", user != null ? user.getEmail() : "unknown@example.com");
        data.put("organization", "org-uuid-here");
        data.put("invoice_number", invoice.getInvoiceNumber());
        data.put("invoice_date", invoice.getInvoiceDate());
        data.put("due_date", invoice.getDueDate());
        data.put("status", invoice.getStatus());
        data.put("currency", invoice.getCurrency());
        data.put("notes", invoice.getNotes() != null ? invoice.getNotes() : "");
        data.put("terms_and_conditions", invoice.getTermsAndConditions() != null ? invoice.getTermsAndConditions() : "");
        data.put("subtotal", String.format("%.2f", invoice.getSubtotal()));
        data.put("discount_total", String.format("%.2f", invoice.getDiscountTotal()));
        data.put("tax_total", String.format("%.2f", invoice.getTaxTotal()));
        data.put("grand_total", String.format("%.2f", invoice.getGrandTotal()));
        data.put("created_at", invoice.getCreatedAt());
        data.put("updated_at", invoice.getUpdatedAt());
        data.put("is_deleted", invoice.isDeleted());
        data.put("deleted_at", invoice.getDeletedAt());

        return data;
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice, User user) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setCustomer(invoice.getCustomerId());
        if (user != null) {
            response.setCustomerName(user.getFirstName() + " " + user.getLastName());
            response.setCreatedByEmail(user.getEmail());
        } else {
            response.setCustomerName("Unknown Customer");
            response.setCreatedByEmail("unknown@example.com");
        }
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setDueDate(invoice.getDueDate());
        response.setStatus(invoice.getStatus());
        response.setCurrency(invoice.getCurrency());
        response.setSubtotal(invoice.getSubtotal());
        response.setDiscountTotal(invoice.getDiscountTotal());
        response.setTaxTotal(invoice.getTaxTotal());
        response.setGrandTotal(invoice.getGrandTotal());
        response.setCreatedAt(invoice.getCreatedAt());
        response.setUpdatedAt(invoice.getUpdatedAt());
        response.setDeleted(invoice.isDeleted());
        response.setDeletedAt(invoice.getDeletedAt());
        return response;
    }
}
