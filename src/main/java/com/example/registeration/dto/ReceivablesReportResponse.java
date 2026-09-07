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
public class ReceivablesReportResponse {
    private LocalDate asOfDate;

    // Aging Buckets Totals
    private BigDecimal totalReceivables;
    private BigDecimal totalOverdue;
    private BigDecimal currentAmount;
    private BigDecimal days1To30;
    private BigDecimal days31To60;
    private BigDecimal days61To90;
    private BigDecimal days90Plus;

    // Customer-wise Aging
    private List<CustomerAgingSummary> customerAgingList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerAgingSummary {
        private UUID customerId;
        private String customerName;
        private String email;
        private String phone;
        private BigDecimal totalBalance;
        private BigDecimal currentAmount;
        private BigDecimal days1To30;
        private BigDecimal days31To60;
        private BigDecimal days61To90;
        private BigDecimal days90Plus;
        private Long openInvoicesCount;
    }
}
