package com.example.registeration.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.registeration.dto.PayablesReportResponse;
import com.example.registeration.dto.PayablesReportResponse.VendorAgingSummary;
import com.example.registeration.dto.ProfitLossReportResponse;
import com.example.registeration.dto.ProfitLossReportResponse.CategoryBreakdown;
import com.example.registeration.dto.ProfitLossReportResponse.MonthlyTrend;
import com.example.registeration.dto.ReceivablesReportResponse;
import com.example.registeration.dto.ReceivablesReportResponse.CustomerAgingSummary;
import com.example.registeration.dto.SalesSummaryReportResponse;
import com.example.registeration.dto.SalesSummaryReportResponse.SalesPeriodData;
import com.example.registeration.dto.SalesSummaryReportResponse.TopCustomerSales;
import com.example.registeration.dto.TaxSummaryReportResponse;
import com.example.registeration.dto.TaxSummaryReportResponse.TaxItemSummary;
import com.example.registeration.entity.*;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.*;

@Service
public class ReportService {

    private final InvoiceRepository invoiceRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final ExpenseRepository expenseRepository;
    private final BillRepository billRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    public ReportService(
            InvoiceRepository invoiceRepository,
            CreditNoteRepository creditNoteRepository,
            ExpenseRepository expenseRepository,
            BillRepository billRepository,
            PurchaseRepository purchaseRepository,
            PaymentRepository paymentRepository,
            CustomerRepository customerRepository,
            VendorRepository vendorRepository,
            UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.expenseRepository = expenseRepository;
        this.billRepository = billRepository;
        this.purchaseRepository = purchaseRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
    }

    private void validateUser(UUID userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private BigDecimal safe(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    // -------------------------------------------------------------
    // 1. PROFIT & LOSS REPORT
    // -------------------------------------------------------------
    public ProfitLossReportResponse getProfitLossReport(LocalDate startDate, LocalDate endDate, UUID userId) {
        validateUser(userId);

        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.withDayOfMonth(1);
        }

        List<Invoice> invoices = invoiceRepository.findByIsDeletedFalseAndInvoiceDateBetween(startDate, endDate);
        List<CreditNote> creditNotes = creditNoteRepository.findByIsDeletedFalseAndCreditNoteDateBetween(startDate, endDate);
        List<Expense> expenses = expenseRepository.findByIsDeletedFalseAndDateBetween(startDate, endDate);
        List<Bill> bills = billRepository.findByIsDeletedFalseAndDateBetween(startDate, endDate);
        List<Purchase> purchases = purchaseRepository.findByIsDeletedFalseAndDateBetween(startDate, endDate);

        // Sales & Returns
        BigDecimal totalSales = invoices.stream()
                .map(i -> safe(i.getGrandTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal salesReturns = creditNotes.stream()
                .map(cn -> safe(cn.getGrandTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netRevenue = totalSales.subtract(salesReturns);

        // Expenses
        BigDecimal operatingExpenses = expenses.stream()
                .map(e -> safe(e.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal billsExpenses = bills.stream()
                .map(b -> safe(b.getTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal purchaseExpenses = purchases.stream()
                .map(p -> safe(p.getTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = operatingExpenses.add(billsExpenses).add(purchaseExpenses);

        // Profits
        BigDecimal grossProfit = netRevenue.subtract(billsExpenses.add(purchaseExpenses));
        BigDecimal netProfit = netRevenue.subtract(totalExpenses);

        BigDecimal netProfitMargin = BigDecimal.ZERO;
        if (netRevenue.compareTo(BigDecimal.ZERO) > 0) {
            netProfitMargin = netProfit.multiply(BigDecimal.valueOf(100))
                    .divide(netRevenue, 2, RoundingMode.HALF_UP);
        }

        // Category breakdown
        Map<String, BigDecimal> categoryMap = new HashMap<>();
        for (Expense e : expenses) {
            String cat = (e.getCategory() != null && !e.getCategory().isBlank()) ? e.getCategory() : "Operating Expense";
            categoryMap.merge(cat, safe(e.getAmount()), BigDecimal::add);
        }
        for (Bill b : bills) {
            String cat = (b.getCategory() != null && !b.getCategory().isBlank()) ? b.getCategory() : "Vendor Bills";
            categoryMap.merge(cat, safe(b.getTotal()), BigDecimal::add);
        }
        for (Purchase p : purchases) {
            String cat = (p.getCategory() != null && !p.getCategory().isBlank()) ? p.getCategory() : "Purchases";
            categoryMap.merge(cat, safe(p.getTotal()), BigDecimal::add);
        }

        List<CategoryBreakdown> expenseCategories = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryMap.entrySet()) {
            double pct = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(BigDecimal.valueOf(100)).divide(totalExpenses, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            expenseCategories.add(new CategoryBreakdown(entry.getKey(), entry.getValue(), pct));
        }
        expenseCategories.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        // Monthly trends
        Map<String, BigDecimal> monthlyRev = new TreeMap<>();
        Map<String, BigDecimal> monthlyExp = new TreeMap<>();
        DateTimeFormatter ymFormat = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Invoice i : invoices) {
            if (i.getInvoiceDate() != null) {
                String ym = i.getInvoiceDate().format(ymFormat);
                monthlyRev.merge(ym, safe(i.getGrandTotal()), BigDecimal::add);
            }
        }
        for (CreditNote cn : creditNotes) {
            if (cn.getCreditNoteDate() != null) {
                String ym = cn.getCreditNoteDate().format(ymFormat);
                monthlyRev.merge(ym, safe(cn.getGrandTotal()).negate(), BigDecimal::add);
            }
        }
        for (Expense e : expenses) {
            if (e.getDate() != null) {
                String ym = e.getDate().format(ymFormat);
                monthlyExp.merge(ym, safe(e.getAmount()), BigDecimal::add);
            }
        }
        for (Bill b : bills) {
            if (b.getDate() != null) {
                String ym = b.getDate().format(ymFormat);
                monthlyExp.merge(ym, safe(b.getTotal()), BigDecimal::add);
            }
        }
        for (Purchase p : purchases) {
            if (p.getDate() != null) {
                String ym = p.getDate().format(ymFormat);
                monthlyExp.merge(ym, safe(p.getTotal()), BigDecimal::add);
            }
        }

        Set<String> allMonths = new TreeSet<>();
        allMonths.addAll(monthlyRev.keySet());
        allMonths.addAll(monthlyExp.keySet());

        List<MonthlyTrend> monthlyTrends = new ArrayList<>();
        for (String m : allMonths) {
            BigDecimal rev = monthlyRev.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal exp = monthlyExp.getOrDefault(m, BigDecimal.ZERO);
            monthlyTrends.add(new MonthlyTrend(m, rev, exp, rev.subtract(exp)));
        }

        return ProfitLossReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalSales(totalSales.setScale(2, RoundingMode.HALF_UP))
                .salesReturns(salesReturns.setScale(2, RoundingMode.HALF_UP))
                .netRevenue(netRevenue.setScale(2, RoundingMode.HALF_UP))
                .operatingExpenses(operatingExpenses.setScale(2, RoundingMode.HALF_UP))
                .billsExpenses(billsExpenses.setScale(2, RoundingMode.HALF_UP))
                .purchaseExpenses(purchaseExpenses.setScale(2, RoundingMode.HALF_UP))
                .totalExpenses(totalExpenses.setScale(2, RoundingMode.HALF_UP))
                .grossProfit(grossProfit.setScale(2, RoundingMode.HALF_UP))
                .netProfit(netProfit.setScale(2, RoundingMode.HALF_UP))
                .netProfitMargin(netProfitMargin)
                .expenseCategories(expenseCategories)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    // -------------------------------------------------------------
    // 2. SALES SUMMARY REPORT
    // -------------------------------------------------------------
    public SalesSummaryReportResponse getSalesSummaryReport(LocalDate startDate, LocalDate endDate, String period, UUID userId) {
        validateUser(userId);

        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.withDayOfMonth(1);
        }
        if (period == null || period.isBlank()) {
            period = "monthly";
        }

        List<Invoice> invoices = invoiceRepository.findByIsDeletedFalseAndInvoiceDateBetween(startDate, endDate);
        List<Payment> allPayments = paymentRepository.findByIsDeletedFalse();

        Map<UUID, BigDecimal> paymentsByInvoice = allPayments.stream()
                .filter(p -> p.getInvoiceId() != null)
                .collect(Collectors.groupingBy(
                        Payment::getInvoiceId,
                        Collectors.reducing(BigDecimal.ZERO, p -> safe(p.getAmount()), BigDecimal::add)
                ));

        Map<UUID, Customer> customerMap = customerRepository.findByIsDeletedFalse().stream()
                .collect(Collectors.toMap(Customer::getId, c -> c, (a, b) -> a));

        long totalInvoices = invoices.size();
        BigDecimal totalInvoicedAmount = BigDecimal.ZERO;
        BigDecimal totalPaidAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;

        for (Invoice i : invoices) {
            totalInvoicedAmount = totalInvoicedAmount.add(safe(i.getGrandTotal()));
            totalDiscountAmount = totalDiscountAmount.add(safe(i.getDiscountTotal()));
            totalTaxAmount = totalTaxAmount.add(safe(i.getTaxTotal()));

            BigDecimal paidForInv = paymentsByInvoice.getOrDefault(i.getId(), BigDecimal.ZERO);
            // If marked as paid but no payment record, treat grandTotal as paid
            if ("Paid".equalsIgnoreCase(i.getStatus()) && paidForInv.compareTo(BigDecimal.ZERO) == 0) {
                paidForInv = safe(i.getGrandTotal());
            }
            totalPaidAmount = totalPaidAmount.add(paidForInv);
        }

        BigDecimal totalOutstandingAmount = totalInvoicedAmount.subtract(totalPaidAmount);
        if (totalOutstandingAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalOutstandingAmount = BigDecimal.ZERO;
        }

        BigDecimal avgInvoice = totalInvoices > 0
                ? totalInvoicedAmount.divide(BigDecimal.valueOf(totalInvoices), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Grouping by Period
        DateTimeFormatter dtf;
        if ("daily".equalsIgnoreCase(period)) {
            dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        } else if ("yearly".equalsIgnoreCase(period)) {
            dtf = DateTimeFormatter.ofPattern("yyyy");
        } else {
            dtf = DateTimeFormatter.ofPattern("yyyy-MM");
        }

        Map<String, List<Invoice>> groupedByPeriod = invoices.stream()
                .filter(i -> i.getInvoiceDate() != null)
                .collect(Collectors.groupingBy(i -> i.getInvoiceDate().format(dtf), TreeMap::new, Collectors.toList()));

        List<SalesPeriodData> salesByPeriod = new ArrayList<>();
        for (Map.Entry<String, List<Invoice>> entry : groupedByPeriod.entrySet()) {
            List<Invoice> invList = entry.getValue();
            long count = invList.size();
            BigDecimal subtotal = invList.stream().map(i -> safe(i.getSubtotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tax = invList.stream().map(i -> safe(i.getTaxTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal discount = invList.stream().map(i -> safe(i.getDiscountTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sales = invList.stream().map(i -> safe(i.getGrandTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);

            salesByPeriod.add(new SalesPeriodData(
                    entry.getKey(),
                    count,
                    subtotal.setScale(2, RoundingMode.HALF_UP),
                    tax.setScale(2, RoundingMode.HALF_UP),
                    discount.setScale(2, RoundingMode.HALF_UP),
                    sales.setScale(2, RoundingMode.HALF_UP)
            ));
        }

        // Top Customers
        Map<UUID, List<Invoice>> invoicesByCustomer = invoices.stream()
                .filter(i -> i.getCustomerId() != null)
                .collect(Collectors.groupingBy(Invoice::getCustomerId));

        List<TopCustomerSales> topCustomers = new ArrayList<>();
        for (Map.Entry<UUID, List<Invoice>> entry : invoicesByCustomer.entrySet()) {
            UUID custId = entry.getKey();
            List<Invoice> custInvoices = entry.getValue();
            Customer cust = customerMap.get(custId);
            String custName = cust != null ? (cust.getDisplayName() != null ? cust.getDisplayName() : cust.getCompanyName()) : "Unknown Customer";

            BigDecimal custSales = custInvoices.stream().map(i -> safe(i.getGrandTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal custPaid = custInvoices.stream().map(i -> paymentsByInvoice.getOrDefault(i.getId(), BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal custBalance = custSales.subtract(custPaid);
            if (custBalance.compareTo(BigDecimal.ZERO) < 0) custBalance = BigDecimal.ZERO;

            topCustomers.add(new TopCustomerSales(
                    custId,
                    custName,
                    (long) custInvoices.size(),
                    custSales.setScale(2, RoundingMode.HALF_UP),
                    custPaid.setScale(2, RoundingMode.HALF_UP),
                    custBalance.setScale(2, RoundingMode.HALF_UP)
            ));
        }
        topCustomers.sort((a, b) -> b.getTotalSales().compareTo(a.getTotalSales()));

        return SalesSummaryReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .period(period)
                .totalInvoices(totalInvoices)
                .totalInvoicedAmount(totalInvoicedAmount.setScale(2, RoundingMode.HALF_UP))
                .totalPaidAmount(totalPaidAmount.setScale(2, RoundingMode.HALF_UP))
                .totalOutstandingAmount(totalOutstandingAmount.setScale(2, RoundingMode.HALF_UP))
                .totalDiscountAmount(totalDiscountAmount.setScale(2, RoundingMode.HALF_UP))
                .totalTaxAmount(totalTaxAmount.setScale(2, RoundingMode.HALF_UP))
                .averageInvoiceValue(avgInvoice)
                .salesByPeriod(salesByPeriod)
                .topCustomers(topCustomers)
                .build();
    }

    // -------------------------------------------------------------
    // 3. GST / TAX SUMMARY REPORT
    // -------------------------------------------------------------
    public TaxSummaryReportResponse getTaxSummaryReport(LocalDate startDate, LocalDate endDate, UUID userId) {
        validateUser(userId);

        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.withDayOfMonth(1);
        }

        List<Invoice> invoices = invoiceRepository.findByIsDeletedFalseAndInvoiceDateBetween(startDate, endDate);
        List<CreditNote> creditNotes = creditNoteRepository.findByIsDeletedFalseAndCreditNoteDateBetween(startDate, endDate);
        List<Bill> bills = billRepository.findByIsDeletedFalseAndDateBetween(startDate, endDate);
        List<Purchase> purchases = purchaseRepository.findByIsDeletedFalseAndDateBetween(startDate, endDate);

        Map<UUID, Customer> customerMap = customerRepository.findByIsDeletedFalse().stream()
                .collect(Collectors.toMap(Customer::getId, c -> c, (a, b) -> a));

        // Output Tax (Sales)
        BigDecimal invoiceTaxable = invoices.stream().map(i -> safe(i.getSubtotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal invoiceTax = invoices.stream().map(i -> safe(i.getTaxTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditNoteTaxable = creditNotes.stream().map(cn -> safe(cn.getSubtotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal creditNoteTax = creditNotes.stream().map(cn -> safe(cn.getTaxTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTaxableSales = invoiceTaxable.subtract(creditNoteTaxable);
        BigDecimal totalOutputTax = invoiceTax.subtract(creditNoteTax);

        // Input Tax (Bills & Purchases)
        BigDecimal billsTaxable = bills.stream().map(b -> safe(b.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal billsTax = bills.stream().map(b -> safe(b.getTax())).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal purchasesTaxable = purchases.stream().map(p -> safe(p.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal purchasesTax = purchases.stream().map(p -> safe(p.getTax())).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTaxablePurchases = billsTaxable.add(purchasesTaxable);
        BigDecimal totalInputTaxCredit = billsTax.add(purchasesTax);

        BigDecimal netTaxPayable = totalOutputTax.subtract(totalInputTaxCredit);

        // Sales Tax Breakdown list
        List<TaxItemSummary> salesTaxBreakdown = new ArrayList<>();
        for (Invoice i : invoices) {
            Customer cust = customerMap.get(i.getCustomerId());
            String partyName = cust != null ? cust.getDisplayName() : "Customer";
            salesTaxBreakdown.add(new TaxItemSummary(
                    "Invoice",
                    i.getInvoiceNumber(),
                    i.getInvoiceDate(),
                    partyName,
                    safe(i.getSubtotal()).setScale(2, RoundingMode.HALF_UP),
                    safe(i.getTaxTotal()).setScale(2, RoundingMode.HALF_UP),
                    safe(i.getGrandTotal()).setScale(2, RoundingMode.HALF_UP)
            ));
        }
        for (CreditNote cn : creditNotes) {
            Customer cust = customerMap.get(cn.getCustomerId());
            String partyName = cust != null ? cust.getDisplayName() : "Customer";
            salesTaxBreakdown.add(new TaxItemSummary(
                    "Credit Note",
                    cn.getCreditNoteNumber(),
                    cn.getCreditNoteDate(),
                    partyName,
                    safe(cn.getSubtotal()).negate().setScale(2, RoundingMode.HALF_UP),
                    safe(cn.getTaxTotal()).negate().setScale(2, RoundingMode.HALF_UP),
                    safe(cn.getGrandTotal()).negate().setScale(2, RoundingMode.HALF_UP)
            ));
        }

        // Purchase Tax Breakdown list
        List<TaxItemSummary> purchaseTaxBreakdown = new ArrayList<>();
        for (Bill b : bills) {
            purchaseTaxBreakdown.add(new TaxItemSummary(
                    "Bill",
                    b.getBillNumber(),
                    b.getDate(),
                    b.getVendor() != null ? b.getVendor() : "Vendor",
                    safe(b.getAmount()).setScale(2, RoundingMode.HALF_UP),
                    safe(b.getTax()).setScale(2, RoundingMode.HALF_UP),
                    safe(b.getTotal()).setScale(2, RoundingMode.HALF_UP)
            ));
        }
        for (Purchase p : purchases) {
            purchaseTaxBreakdown.add(new TaxItemSummary(
                    "Purchase",
                    p.getPurchaseNumber(),
                    p.getDate(),
                    p.getVendor() != null ? p.getVendor() : "Vendor",
                    safe(p.getAmount()).setScale(2, RoundingMode.HALF_UP),
                    safe(p.getTax()).setScale(2, RoundingMode.HALF_UP),
                    safe(p.getTotal()).setScale(2, RoundingMode.HALF_UP)
            ));
        }

        return TaxSummaryReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalTaxableSales(totalTaxableSales.setScale(2, RoundingMode.HALF_UP))
                .totalOutputTax(totalOutputTax.setScale(2, RoundingMode.HALF_UP))
                .totalTaxablePurchases(totalTaxablePurchases.setScale(2, RoundingMode.HALF_UP))
                .totalInputTaxCredit(totalInputTaxCredit.setScale(2, RoundingMode.HALF_UP))
                .netTaxPayable(netTaxPayable.setScale(2, RoundingMode.HALF_UP))
                .salesTaxBreakdown(salesTaxBreakdown)
                .purchaseTaxBreakdown(purchaseTaxBreakdown)
                .build();
    }

    // -------------------------------------------------------------
    // 4. RECEIVABLES AGING REPORT
    // -------------------------------------------------------------
    public ReceivablesReportResponse getReceivablesReport(LocalDate asOfDate, UUID userId) {
        validateUser(userId);

        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }

        List<Invoice> invoices = invoiceRepository.findByIsDeletedFalse();
        List<Payment> payments = paymentRepository.findByIsDeletedFalse();
        Map<UUID, Customer> customerMap = customerRepository.findByIsDeletedFalse().stream()
                .collect(Collectors.toMap(Customer::getId, c -> c, (a, b) -> a));

        Map<UUID, BigDecimal> paymentsByInvoice = payments.stream()
                .filter(p -> p.getInvoiceId() != null)
                .collect(Collectors.groupingBy(
                        Payment::getInvoiceId,
                        Collectors.reducing(BigDecimal.ZERO, p -> safe(p.getAmount()), BigDecimal::add)
                ));

        BigDecimal totalReceivables = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        BigDecimal currentAmount = BigDecimal.ZERO;
        BigDecimal days1To30 = BigDecimal.ZERO;
        BigDecimal days31To60 = BigDecimal.ZERO;
        BigDecimal days61To90 = BigDecimal.ZERO;
        BigDecimal days90Plus = BigDecimal.ZERO;

        Map<UUID, CustomerAgingSummary> agingByCustomer = new HashMap<>();

        for (Invoice i : invoices) {
            if ("Cancelled".equalsIgnoreCase(i.getStatus())) {
                continue;
            }

            BigDecimal grandTotal = safe(i.getGrandTotal());
            BigDecimal paid = paymentsByInvoice.getOrDefault(i.getId(), BigDecimal.ZERO);
            if ("Paid".equalsIgnoreCase(i.getStatus()) && paid.compareTo(BigDecimal.ZERO) == 0) {
                paid = grandTotal;
            }

            BigDecimal balance = grandTotal.subtract(paid);
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            totalReceivables = totalReceivables.add(balance);

            LocalDate due = i.getDueDate() != null ? i.getDueDate() : i.getInvoiceDate();
            if (due == null) {
                due = asOfDate;
            }
            long days = ChronoUnit.DAYS.between(due, asOfDate);

            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal d1_30 = BigDecimal.ZERO;
            BigDecimal d31_60 = BigDecimal.ZERO;
            BigDecimal d61_90 = BigDecimal.ZERO;
            BigDecimal d90_plus = BigDecimal.ZERO;

            if (days <= 0) {
                currentAmount = currentAmount.add(balance);
                cur = balance;
            } else {
                totalOverdue = totalOverdue.add(balance);
                if (days <= 30) {
                    days1To30 = days1To30.add(balance);
                    d1_30 = balance;
                } else if (days <= 60) {
                    days31To60 = days31To60.add(balance);
                    d31_60 = balance;
                } else if (days <= 90) {
                    days61To90 = days61To90.add(balance);
                    d61_90 = balance;
                } else {
                    days90Plus = days90Plus.add(balance);
                    d90_plus = balance;
                }
            }

            UUID custId = i.getCustomerId();
            if (custId != null) {
                Customer cust = customerMap.get(custId);
                String name = cust != null ? cust.getDisplayName() : "Customer " + custId;
                String email = cust != null ? cust.getEmail() : null;
                String phone = cust != null ? cust.getPrimaryPhone() : null;

                CustomerAgingSummary summary = agingByCustomer.computeIfAbsent(custId, id ->
                        CustomerAgingSummary.builder()
                                .customerId(id)
                                .customerName(name)
                                .email(email)
                                .phone(phone)
                                .totalBalance(BigDecimal.ZERO)
                                .currentAmount(BigDecimal.ZERO)
                                .days1To30(BigDecimal.ZERO)
                                .days31To60(BigDecimal.ZERO)
                                .days61To90(BigDecimal.ZERO)
                                .days90Plus(BigDecimal.ZERO)
                                .openInvoicesCount(0L)
                                .build()
                );

                summary.setTotalBalance(summary.getTotalBalance().add(balance));
                summary.setCurrentAmount(summary.getCurrentAmount().add(cur));
                summary.setDays1To30(summary.getDays1To30().add(d1_30));
                summary.setDays31To60(summary.getDays31To60().add(d31_60));
                summary.setDays61To90(summary.getDays61To90().add(d61_90));
                summary.setDays90Plus(summary.getDays90Plus().add(d90_plus));
                summary.setOpenInvoicesCount(summary.getOpenInvoicesCount() + 1);
            }
        }

        List<CustomerAgingSummary> customerList = new ArrayList<>(agingByCustomer.values());
        customerList.sort((a, b) -> b.getTotalBalance().compareTo(a.getTotalBalance()));

        return ReceivablesReportResponse.builder()
                .asOfDate(asOfDate)
                .totalReceivables(totalReceivables.setScale(2, RoundingMode.HALF_UP))
                .totalOverdue(totalOverdue.setScale(2, RoundingMode.HALF_UP))
                .currentAmount(currentAmount.setScale(2, RoundingMode.HALF_UP))
                .days1To30(days1To30.setScale(2, RoundingMode.HALF_UP))
                .days31To60(days31To60.setScale(2, RoundingMode.HALF_UP))
                .days61To90(days61To90.setScale(2, RoundingMode.HALF_UP))
                .days90Plus(days90Plus.setScale(2, RoundingMode.HALF_UP))
                .customerAgingList(customerList)
                .build();
    }

    // -------------------------------------------------------------
    // 5. PAYABLES REPORT
    // -------------------------------------------------------------
    public PayablesReportResponse getPayablesReport(LocalDate asOfDate, UUID userId) {
        validateUser(userId);

        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }

        List<Bill> bills = billRepository.findByIsDeletedFalse();
        List<Purchase> purchases = purchaseRepository.findByIsDeletedFalse();

        BigDecimal totalPayables = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        BigDecimal currentAmount = BigDecimal.ZERO;
        BigDecimal days1To30 = BigDecimal.ZERO;
        BigDecimal days31To60 = BigDecimal.ZERO;
        BigDecimal days61To90 = BigDecimal.ZERO;
        BigDecimal days90Plus = BigDecimal.ZERO;

        Map<String, VendorAgingSummary> vendorMap = new HashMap<>();

        // Process Bills
        for (Bill b : bills) {
            if ("Paid".equalsIgnoreCase(b.getStatus()) || "Cancelled".equalsIgnoreCase(b.getStatus())) {
                continue;
            }

            BigDecimal balance = safe(b.getTotal());
            if (balance.compareTo(BigDecimal.ZERO) <= 0) continue;

            totalPayables = totalPayables.add(balance);

            LocalDate due = b.getDueDate() != null ? b.getDueDate() : b.getDate();
            if (due == null) due = asOfDate;
            long days = ChronoUnit.DAYS.between(due, asOfDate);

            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal d1_30 = BigDecimal.ZERO;
            BigDecimal d31_60 = BigDecimal.ZERO;
            BigDecimal d61_90 = BigDecimal.ZERO;
            BigDecimal d90_plus = BigDecimal.ZERO;

            if (days <= 0) {
                currentAmount = currentAmount.add(balance);
                cur = balance;
            } else {
                totalOverdue = totalOverdue.add(balance);
                if (days <= 30) {
                    days1To30 = days1To30.add(balance);
                    d1_30 = balance;
                } else if (days <= 60) {
                    days31To60 = days31To60.add(balance);
                    d31_60 = balance;
                } else if (days <= 90) {
                    days61To90 = days61To90.add(balance);
                    d61_90 = balance;
                } else {
                    days90Plus = days90Plus.add(balance);
                    d90_plus = balance;
                }
            }

            String vName = b.getVendor() != null && !b.getVendor().isBlank() ? b.getVendor() : "Unknown Vendor";
            VendorAgingSummary summary = vendorMap.computeIfAbsent(vName, name ->
                    VendorAgingSummary.builder()
                            .vendorName(name)
                            .totalPayable(BigDecimal.ZERO)
                            .currentAmount(BigDecimal.ZERO)
                            .days1To30(BigDecimal.ZERO)
                            .days31To60(BigDecimal.ZERO)
                            .days61To90(BigDecimal.ZERO)
                            .days90Plus(BigDecimal.ZERO)
                            .pendingBillsCount(0L)
                            .build()
            );

            summary.setTotalPayable(summary.getTotalPayable().add(balance));
            summary.setCurrentAmount(summary.getCurrentAmount().add(cur));
            summary.setDays1To30(summary.getDays1To30().add(d1_30));
            summary.setDays31To60(summary.getDays31To60().add(d31_60));
            summary.setDays61To90(summary.getDays61To90().add(d61_90));
            summary.setDays90Plus(summary.getDays90Plus().add(d90_plus));
            summary.setPendingBillsCount(summary.getPendingBillsCount() + 1);
        }

        // Process Purchases
        for (Purchase p : purchases) {
            if ("Paid".equalsIgnoreCase(p.getStatus()) || "Cancelled".equalsIgnoreCase(p.getStatus())) {
                continue;
            }

            BigDecimal balance = safe(p.getTotal());
            if (balance.compareTo(BigDecimal.ZERO) <= 0) continue;

            totalPayables = totalPayables.add(balance);

            LocalDate due = p.getDate() != null ? p.getDate() : asOfDate;
            long days = ChronoUnit.DAYS.between(due, asOfDate);

            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal d1_30 = BigDecimal.ZERO;
            BigDecimal d31_60 = BigDecimal.ZERO;
            BigDecimal d61_90 = BigDecimal.ZERO;
            BigDecimal d90_plus = BigDecimal.ZERO;

            if (days <= 0) {
                currentAmount = currentAmount.add(balance);
                cur = balance;
            } else {
                totalOverdue = totalOverdue.add(balance);
                if (days <= 30) {
                    days1To30 = days1To30.add(balance);
                    d1_30 = balance;
                } else if (days <= 60) {
                    days31To60 = days31To60.add(balance);
                    d31_60 = balance;
                } else if (days <= 90) {
                    days61To90 = days61To90.add(balance);
                    d61_90 = balance;
                } else {
                    days90Plus = days90Plus.add(balance);
                    d90_plus = balance;
                }
            }

            String vName = p.getVendor() != null && !p.getVendor().isBlank() ? p.getVendor() : "Unknown Vendor";
            VendorAgingSummary summary = vendorMap.computeIfAbsent(vName, name ->
                    VendorAgingSummary.builder()
                            .vendorName(name)
                            .totalPayable(BigDecimal.ZERO)
                            .currentAmount(BigDecimal.ZERO)
                            .days1To30(BigDecimal.ZERO)
                            .days31To60(BigDecimal.ZERO)
                            .days61To90(BigDecimal.ZERO)
                            .days90Plus(BigDecimal.ZERO)
                            .pendingBillsCount(0L)
                            .build()
            );

            summary.setTotalPayable(summary.getTotalPayable().add(balance));
            summary.setCurrentAmount(summary.getCurrentAmount().add(cur));
            summary.setDays1To30(summary.getDays1To30().add(d1_30));
            summary.setDays31To60(summary.getDays31To60().add(d31_60));
            summary.setDays61To90(summary.getDays61To90().add(d61_90));
            summary.setDays90Plus(summary.getDays90Plus().add(d90_plus));
            summary.setPendingBillsCount(summary.getPendingBillsCount() + 1);
        }

        List<VendorAgingSummary> vendorList = new ArrayList<>(vendorMap.values());
        vendorList.sort((a, b) -> b.getTotalPayable().compareTo(a.getTotalPayable()));

        return PayablesReportResponse.builder()
                .asOfDate(asOfDate)
                .totalPayables(totalPayables.setScale(2, RoundingMode.HALF_UP))
                .totalOverdue(totalOverdue.setScale(2, RoundingMode.HALF_UP))
                .currentAmount(currentAmount.setScale(2, RoundingMode.HALF_UP))
                .days1To30(days1To30.setScale(2, RoundingMode.HALF_UP))
                .days31To60(days31To60.setScale(2, RoundingMode.HALF_UP))
                .days61To90(days61To90.setScale(2, RoundingMode.HALF_UP))
                .days90Plus(days90Plus.setScale(2, RoundingMode.HALF_UP))
                .vendorAgingList(vendorList)
                .build();
    }
}
