package com.example.registeration.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.example.registeration.entity.Customer;
import com.example.registeration.entity.Invoice;
import com.example.registeration.entity.InvoiceItem;
import com.example.registeration.entity.Product;
import com.example.registeration.entity.User;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.CustomerRepository;
import com.example.registeration.repository.InvoiceItemRepository;
import com.example.registeration.repository.InvoiceRepository;
import com.example.registeration.repository.ProductRepository;
import com.example.registeration.repository.UserRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceItemRepository invoiceItemRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository) {

        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        // Validate customer in CustomerRepository or UserRepository
        UUID customerId = request.getCustomer();
        String customerName = resolveCustomerName(customerId);
        String customerEmail = resolveCustomerEmail(customerId);

        if (!customerRepository.existsById(customerId) && !userRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        long invoiceCount = invoiceRepository.count();
        String invoiceNumber = String.format("INV-%06d", invoiceCount + 1);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomerId(customerId);
        invoice.setInvoiceDate(request.getInvoiceDate() != null ? request.getInvoiceDate() : LocalDate.now());
        invoice.setDueDate(request.getDueDate());
        invoice.setStatus("draft");
        invoice.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
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
                BigDecimal unitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice()
                        : BigDecimal.valueOf(1000.00);
                BigDecimal taxPercent = itemReq.getTax() != null ? itemReq.getTax() : BigDecimal.valueOf(18.00);
                BigDecimal discount = itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO;
                BigDecimal quantity = itemReq.getQuantity() != null ? itemReq.getQuantity() : BigDecimal.ONE;

                BigDecimal itemSubtotal = unitPrice.multiply(quantity);
                BigDecimal taxableAmount = itemSubtotal.subtract(discount);
                if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
                    taxableAmount = BigDecimal.ZERO;
                }
                BigDecimal taxVal = taxableAmount.multiply(taxPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
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

        return mapToInvoiceResponse(savedInvoice, customerName, customerEmail);
    }

    public InvoiceResponse getInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        return mapToInvoiceResponse(invoice);
    }

    public Page<InvoiceResponse> getInvoices(
            String search,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int pageSize) {

        int pageIndex = Math.max(0, page - 1);
        int validPageSize = pageSize > 0 ? pageSize : 10;
        Pageable pageable = PageRequest.of(pageIndex, validPageSize);
        Page<Invoice> invoices = invoiceRepository.findAllInvoices(search, status, startDate, endDate, pageable);

        List<InvoiceResponse> content = invoices.getContent().stream()
                .map(this::mapToInvoiceResponse)
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

        return mapToInvoiceResponse(updatedInvoice);
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

        return mapToInvoiceResponse(saved);
    }

    public Map<String, Object> getInvoiceDetails(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
        Map<String, Object> customerMap = resolveCustomerDetails(invoice.getCustomerId());

        List<Map<String, Object>> itemsList = items.stream().map(item -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("id", item.getId());

            Map<String, Object> productMap = new LinkedHashMap<>();
            productMap.put("id", item.getProductId());
            if (item.getProductId() != null) {
                Optional<Product> prodOpt = productRepository.findById(item.getProductId());
                if (prodOpt.isPresent()) {
                    Product p = prodOpt.get();
                    productMap.put("name", p.getName() != null ? p.getName() : "Item");
                    productMap.put("sku", p.getSku() != null ? p.getSku() : "");
                    productMap.put("product_type", p.getProductType() != null ? p.getProductType() : "goods");
                } else {
                    productMap.put("name", "Product (" + item.getProductId() + ")");
                    productMap.put("sku", "");
                    productMap.put("product_type", "goods");
                }
            } else {
                productMap.put("name", "Item");
                productMap.put("sku", "");
                productMap.put("product_type", "goods");
            }
            itemMap.put("product", productMap);

            itemMap.put("description", item.getDescription() != null ? item.getDescription() : "");
            itemMap.put("quantity", formatDecimal(item.getQuantity()));
            itemMap.put("unit_price", formatDecimal(item.getUnitPrice()));
            itemMap.put("discount", formatDecimal(item.getDiscount()));
            itemMap.put("tax", formatDecimal(item.getTax()));
            itemMap.put("line_total", formatDecimal(item.getLineTotal()));
            return itemMap;
        }).collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", invoice.getId());
        data.put("customer", customerMap);
        data.put("items", itemsList);
        data.put("created_by", customerMap.getOrDefault("email", "admin@example.com"));
        data.put("organization", "default");
        data.put("invoice_number", invoice.getInvoiceNumber());
        data.put("invoice_date", invoice.getInvoiceDate());
        data.put("due_date", invoice.getDueDate());
        data.put("status", invoice.getStatus());
        data.put("currency", invoice.getCurrency());
        data.put("notes", invoice.getNotes() != null ? invoice.getNotes() : "");
        data.put("terms_and_conditions",
                invoice.getTermsAndConditions() != null ? invoice.getTermsAndConditions() : "");
        data.put("subtotal", formatDecimal(invoice.getSubtotal()));
        data.put("discount_total", formatDecimal(invoice.getDiscountTotal()));
        data.put("tax_total", formatDecimal(invoice.getTaxTotal()));
        data.put("grand_total", formatDecimal(invoice.getGrandTotal()));
        data.put("created_at", invoice.getCreatedAt());
        data.put("updated_at", invoice.getUpdatedAt());
        data.put("is_deleted", invoice.isDeleted());
        data.put("deleted_at", invoice.getDeletedAt());

        return data;
    }

    private Map<String, Object> resolveCustomerDetails(UUID customerId) {
        Map<String, Object> customerMap = new LinkedHashMap<>();
        if (customerId == null) {
            customerMap.put("id", null);
            customerMap.put("display_name", "Unknown Customer");
            customerMap.put("company_name", "Unknown Inc");
            customerMap.put("email", "unknown@example.com");
            return customerMap;
        }

        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isPresent()) {
            Customer c = customerOpt.get();
            customerMap.put("id", c.getId());
            String displayName = c.getDisplayName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = (c.getFirstName() != null ? c.getFirstName() : "") + " " + (c.getLastName() != null ? c.getLastName() : "");
                displayName = displayName.trim();
                if (displayName.isEmpty()) displayName = c.getCompanyName() != null ? c.getCompanyName() : "Customer";
            }
            customerMap.put("display_name", displayName);
            customerMap.put("company_name", c.getCompanyName() != null ? c.getCompanyName() : "");
            customerMap.put("email", c.getEmail() != null ? c.getEmail() : "");
            return customerMap;
        }

        Optional<User> userOpt = userRepository.findById(customerId);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            customerMap.put("id", u.getId());
            customerMap.put("display_name", (u.getFirstName() != null ? u.getFirstName() : "") + " " + (u.getLastName() != null ? u.getLastName() : ""));
            customerMap.put("company_name", (u.getFirstName() != null ? u.getFirstName() : "Customer") + " Inc");
            customerMap.put("email", u.getEmail() != null ? u.getEmail() : "");
            return customerMap;
        }

        customerMap.put("id", customerId);
        customerMap.put("display_name", "Unknown Customer");
        customerMap.put("company_name", "Unknown Inc");
        customerMap.put("email", "unknown@example.com");
        return customerMap;
    }

    private String resolveCustomerName(UUID customerId) {
        if (customerId == null) return "Unknown Customer";
        Optional<Customer> cust = customerRepository.findById(customerId);
        if (cust.isPresent()) {
            Customer c = cust.get();
            if (c.getDisplayName() != null && !c.getDisplayName().isEmpty()) return c.getDisplayName();
            if (c.getCompanyName() != null && !c.getCompanyName().isEmpty()) return c.getCompanyName();
            String name = (c.getFirstName() != null ? c.getFirstName() : "") + " " + (c.getLastName() != null ? c.getLastName() : "");
            return name.trim().isEmpty() ? "Customer" : name.trim();
        }
        Optional<User> user = userRepository.findById(customerId);
        if (user.isPresent()) {
            User u = user.get();
            return (u.getFirstName() != null ? u.getFirstName() : "") + " " + (u.getLastName() != null ? u.getLastName() : "");
        }
        return "Unknown Customer";
    }

    private String resolveCustomerEmail(UUID customerId) {
        if (customerId == null) return "unknown@example.com";
        Optional<Customer> cust = customerRepository.findById(customerId);
        if (cust.isPresent() && cust.get().getEmail() != null) {
            return cust.get().getEmail();
        }
        Optional<User> user = userRepository.findById(customerId);
        if (user.isPresent() && user.get().getEmail() != null) {
            return user.get().getEmail();
        }
        return "unknown@example.com";
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        String customerName = resolveCustomerName(invoice.getCustomerId());
        String customerEmail = resolveCustomerEmail(invoice.getCustomerId());
        return mapToInvoiceResponse(invoice, customerName, customerEmail);
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice, String customerName, String customerEmail) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setCustomer(invoice.getCustomerId());
        response.setCustomerName(customerName);
        response.setCreatedByEmail(customerEmail);
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

    private String formatDecimal(BigDecimal val) {
        return val != null ? String.format("%.2f", val) : "0.00";
    }
}
