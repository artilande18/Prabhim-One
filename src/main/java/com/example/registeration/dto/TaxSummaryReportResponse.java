package com.example.registeration.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxSummaryReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;

    // Output Tax (Sales)
    private BigDecimal totalTaxableSales;
    private BigDecimal totalOutputTax;

    // Input Tax (Purchases/Bills)
    private BigDecimal totalTaxablePurchases;
    private BigDecimal totalInputTaxCredit;

    // Net Tax Liability
    private BigDecimal netTaxPayable;

    // Output Tax Breakdown (Invoices & Credit Notes)
    private List<TaxItemSummary> salesTaxBreakdown;

    // Input Tax Breakdown (Bills & Purchases)
    private List<TaxItemSummary> purchaseTaxBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxItemSummary {
        private String transactionType;
        private String referenceNumber;
        private LocalDate date;
        private String partyName;
        private BigDecimal taxableAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
    }
}
