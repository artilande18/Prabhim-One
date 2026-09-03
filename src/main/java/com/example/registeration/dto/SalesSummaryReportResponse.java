package com.example.registeration.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private String period;

    // Summary metrics
    private Long totalInvoices;
    private BigDecimal totalInvoicedAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalOutstandingAmount;
    private BigDecimal totalDiscountAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal averageInvoiceValue;

    // Periodic breakdown
    private List<SalesPeriodData> salesByPeriod;

    // Top Customers
    private List<TopCustomerSales> topCustomers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesPeriodData {
        private String periodLabel;
        private Long invoiceCount;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal discount;
        private BigDecimal totalSales;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomerSales {
        private UUID customerId;
        private String customerName;
        private Long invoiceCount;
        private BigDecimal totalSales;
        private BigDecimal totalPaid;
        private BigDecimal balanceDue;
    }
}
