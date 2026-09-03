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
public class ProfitLossReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;

    // Income
    private BigDecimal totalSales;
    private BigDecimal salesReturns;
    private BigDecimal netRevenue;

    // Expenses
    private BigDecimal operatingExpenses;
    private BigDecimal billsExpenses;
    private BigDecimal purchaseExpenses;
    private BigDecimal totalExpenses;

    // Profit
    private BigDecimal grossProfit;
    private BigDecimal netProfit;
    private BigDecimal netProfitMargin;

    // Categorized breakdowns
    private List<CategoryBreakdown> expenseCategories;
    private List<MonthlyTrend> monthlyTrends;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private BigDecimal amount;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private String period;
        private BigDecimal revenue;
        private BigDecimal expenses;
        private BigDecimal netProfit;
    }
}
