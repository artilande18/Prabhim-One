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

import com.example.registeration.dto.CreateCreditNoteRequest;
import com.example.registeration.dto.CreditNoteItemRequest;
import com.example.registeration.dto.CreditNoteResponse;
import com.example.registeration.dto.UpdateCreditNoteRequest;
import com.example.registeration.entity.CreditNote;
import com.example.registeration.entity.CreditNoteItem;
import com.example.registeration.entity.Customer;
import com.example.registeration.entity.Invoice;
import com.example.registeration.entity.Product;
import com.example.registeration.entity.User;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.CreditNoteItemRepository;
import com.example.registeration.repository.CreditNoteRepository;
import com.example.registeration.repository.CustomerRepository;
import com.example.registeration.repository.InvoiceRepository;
import com.example.registeration.repository.ProductRepository;
import com.example.registeration.repository.UserRepository;

@Service
public class CreditNoteService {

    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteItemRepository creditNoteItemRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;

    public CreditNoteService(
            CreditNoteRepository creditNoteRepository,
            CreditNoteItemRepository creditNoteItemRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            InvoiceRepository invoiceRepository,
            ProductRepository productRepository) {

        this.creditNoteRepository = creditNoteRepository;
        this.creditNoteItemRepository = creditNoteItemRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CreditNoteResponse createCreditNote(CreateCreditNoteRequest request) {
        // Validate customer exists in customer repo or user repo
        String customerName = resolveCustomerName(request.getCustomer());
        if ("Customer".equals(customerName) && !userRepository.existsById(request.getCustomer()) && !customerRepository.existsById(request.getCustomer())) {
            throw new ResourceNotFoundException("Customer not found with id: " + request.getCustomer());
        }

        long count = creditNoteRepository.count();
        String creditNoteNumber = String.format("CN-%06d", count + 1);

        CreditNote creditNote = new CreditNote();
        creditNote.setCreditNoteNumber(creditNoteNumber);
        creditNote.setCustomerId(request.getCustomer());
        creditNote.setInvoiceId(request.getInvoice());
        creditNote.setCreditNoteDate(request.getCreditNoteDate() != null ? request.getCreditNoteDate() : LocalDate.now());
        creditNote.setStatus("draft");
        creditNote.setCurrency(request.getCurrency());
        creditNote.setReason(request.getReason());
        creditNote.setNotes(request.getNotes());
        creditNote.setTermsAndConditions(request.getTermsAndConditions());
        creditNote.setDeleted(false);
        creditNote.setCreatedAt(LocalDateTime.now());
        creditNote.setUpdatedAt(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        List<CreditNoteItem> creditNoteItems = new ArrayList<>();

        if (request.getItems() != null) {
            for (CreditNoteItemRequest itemReq : request.getItems()) {
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

                CreditNoteItem item = new CreditNoteItem();
                item.setProductId(itemReq.getProductId());
                item.setDescription(itemReq.getDescription() != null ? itemReq.getDescription() : "");
                item.setQuantity(quantity);
                item.setUnitPrice(unitPrice);
                item.setDiscount(discount);
                item.setTax(taxPercent);
                item.setLineTotal(lineTotal);
                creditNoteItems.add(item);
            }
        }

        creditNote.setSubtotal(subtotal);
        creditNote.setDiscountTotal(discountTotal);
        creditNote.setTaxTotal(taxTotal);
        creditNote.setGrandTotal(grandTotal);

        CreditNote saved = creditNoteRepository.save(creditNote);

        for (CreditNoteItem item : creditNoteItems) {
            item.setCreditNoteId(saved.getId());
            creditNoteItemRepository.save(item);
        }

        return mapToCreditNoteResponse(saved);
    }

    public Page<CreditNoteResponse> getCreditNotes(
            String search,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int pageSize) {

        int pageIndex = Math.max(0, page - 1);
        int size = (pageSize <= 0) ? 10 : pageSize;
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<CreditNote> creditNotesPage = creditNoteRepository.findWithFilters(
                search,
                status,
                startDate,
                endDate,
                pageable
        );

        List<CreditNoteResponse> responses = creditNotesPage.getContent().stream()
                .map(this::mapToCreditNoteResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, creditNotesPage.getTotalElements());
    }

    public CreditNoteResponse getCreditNote(UUID id) {
        CreditNote creditNote = creditNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));

        return mapToCreditNoteResponse(creditNote);
    }

    public Map<String, Object> getCreditNoteDetails(UUID id) {
        CreditNote creditNote = creditNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));

        List<CreditNoteItem> items = creditNoteItemRepository.findByCreditNoteId(id);

        Map<String, Object> customerMap = new LinkedHashMap<>();
        customerMap.put("id", creditNote.getCustomerId());
        customerMap.put("display_name", resolveCustomerName(creditNote.getCustomerId()));

        Customer cust = customerRepository.findById(creditNote.getCustomerId()).orElse(null);
        if (cust != null) {
            customerMap.put("company_name", cust.getCompanyName() != null ? cust.getCompanyName() : "");
            customerMap.put("email", cust.getEmail() != null ? cust.getEmail() : "");
        } else {
            User u = userRepository.findById(creditNote.getCustomerId()).orElse(null);
            customerMap.put("company_name", u != null ? u.getFirstName() + " Inc" : "");
            customerMap.put("email", u != null ? u.getEmail() : "");
        }

        List<Map<String, Object>> itemsList = items.stream().map(item -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("id", item.getId());

            Map<String, Object> productMap = new LinkedHashMap<>();
            productMap.put("id", item.getProductId());

            if (item.getProductId() != null) {
                Product p = productRepository.findById(item.getProductId()).orElse(null);
                if (p != null) {
                    productMap.put("name", p.getName());
                    productMap.put("sku", p.getSku());
                    productMap.put("product_type", p.getProductType());
                } else {
                    productMap.put("name", "Product Item");
                    productMap.put("sku", "SKU-ITEM");
                    productMap.put("product_type", "goods");
                }
            } else {
                productMap.put("name", "Product Item");
                productMap.put("sku", "SKU-ITEM");
                productMap.put("product_type", "goods");
            }
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
        data.put("id", creditNote.getId());
        data.put("credit_note_number", creditNote.getCreditNoteNumber());
        data.put("customer", customerMap);
        data.put("invoice_id", creditNote.getInvoiceId());
        data.put("credit_note_date", creditNote.getCreditNoteDate());
        data.put("status", creditNote.getStatus());
        data.put("currency", creditNote.getCurrency());
        data.put("reason", creditNote.getReason());
        data.put("subtotal", creditNote.getSubtotal());
        data.put("discount_total", creditNote.getDiscountTotal());
        data.put("tax_total", creditNote.getTaxTotal());
        data.put("grand_total", creditNote.getGrandTotal());
        data.put("notes", creditNote.getNotes());
        data.put("terms_and_conditions", creditNote.getTermsAndConditions());
        data.put("items", itemsList);
        data.put("created_at", creditNote.getCreatedAt());
        data.put("updated_at", creditNote.getUpdatedAt());

        return data;
    }

    @Transactional
    public CreditNoteResponse updateCreditNote(UUID id, UpdateCreditNoteRequest request) {
        CreditNote creditNote = creditNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));

        if (request.getCreditNoteDate() != null) {
            creditNote.setCreditNoteDate(request.getCreditNoteDate());
        }
        if (request.getCurrency() != null && !request.getCurrency().isEmpty()) {
            creditNote.setCurrency(request.getCurrency());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            creditNote.setStatus(request.getStatus());
        }
        if (request.getReason() != null) {
            creditNote.setReason(request.getReason());
        }
        if (request.getNotes() != null) {
            creditNote.setNotes(request.getNotes());
        }
        if (request.getTermsAndConditions() != null) {
            creditNote.setTermsAndConditions(request.getTermsAndConditions());
        }

        creditNote.setUpdatedAt(LocalDateTime.now());
        CreditNote updated = creditNoteRepository.save(creditNote);
        return mapToCreditNoteResponse(updated);
    }

    @Transactional
    public void deleteCreditNote(UUID id) {
        CreditNote creditNote = creditNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));

        creditNote.setDeleted(true);
        creditNote.setDeletedAt(LocalDateTime.now());
        creditNote.setUpdatedAt(LocalDateTime.now());
        creditNoteRepository.save(creditNote);
    }

    private CreditNoteResponse mapToCreditNoteResponse(CreditNote creditNote) {
        CreditNoteResponse res = new CreditNoteResponse();
        res.setId(creditNote.getId());
        res.setCreditNoteNumber(creditNote.getCreditNoteNumber());
        res.setCustomer(creditNote.getCustomerId());
        res.setCustomerName(resolveCustomerName(creditNote.getCustomerId()));
        res.setInvoiceId(creditNote.getInvoiceId());
        if (creditNote.getInvoiceId() != null) {
            Invoice inv = invoiceRepository.findById(creditNote.getInvoiceId()).orElse(null);
            if (inv != null) {
                res.setInvoiceNumber(inv.getInvoiceNumber());
            }
        }
        res.setCreditNoteDate(creditNote.getCreditNoteDate());
        res.setStatus(creditNote.getStatus());
        res.setCurrency(creditNote.getCurrency());
        res.setReason(creditNote.getReason());
        res.setSubtotal(creditNote.getSubtotal());
        res.setDiscountTotal(creditNote.getDiscountTotal());
        res.setTaxTotal(creditNote.getTaxTotal());
        res.setGrandTotal(creditNote.getGrandTotal());
        res.setCreatedByEmail("admin@prabhimtechnologies.in");
        res.setCreatedAt(creditNote.getCreatedAt());
        res.setUpdatedAt(creditNote.getUpdatedAt());
        res.setDeleted(creditNote.isDeleted());
        res.setDeletedAt(creditNote.getDeletedAt());
        return res;
    }

    private String resolveCustomerName(UUID customerId) {
        if (customerId == null) return "Customer";

        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null) {
            if (customer.getDisplayName() != null && !customer.getDisplayName().isEmpty()) {
                return customer.getDisplayName();
            }
            if (customer.getCompanyName() != null && !customer.getCompanyName().isEmpty()) {
                return customer.getCompanyName();
            }
            String first = customer.getFirstName() != null ? customer.getFirstName() : "";
            String last = customer.getLastName() != null ? customer.getLastName() : "";
            String name = (first + " " + last).trim();
            if (!name.isEmpty()) return name;
        }

        User user = userRepository.findById(customerId).orElse(null);
        if (user != null) {
            String first = user.getFirstName() != null ? user.getFirstName() : "";
            String last = user.getLastName() != null ? user.getLastName() : "";
            String name = (first + " " + last).trim();
            if (!name.isEmpty()) return name;
            if (user.getEmail() != null) return user.getEmail();
        }

        return "Customer";
    }
}
