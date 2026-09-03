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
public class PayablesReportResponse {
    private LocalDate asOfDate;

    // Aging Buckets Totals
    private BigDecimal totalPayables;
    private BigDecimal totalOverdue;
    private BigDecimal currentAmount;
    private BigDecimal days1To30;
    private BigDecimal days31To60;
    private BigDecimal days61To90;
    private BigDecimal days90Plus;

    // Vendor-wise Aging
    private List<VendorAgingSummary> vendorAgingList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorAgingSummary {
        private String vendorName;
        private BigDecimal totalPayable;
        private BigDecimal currentAmount;
        private BigDecimal days1To30;
        private BigDecimal days31To60;
        private BigDecimal days61To90;
        private BigDecimal days90Plus;
        private Long pendingBillsCount;
    }
}
