package com.example.registeration.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubscriptionPlanDTO {

    private String id;
    private String name;
    private String description;

    @JsonProperty("monthly_price")
    private BigDecimal monthlyPrice;

    @JsonProperty("yearly_price")
    private BigDecimal yearlyPrice;

    private String currency;

    @JsonProperty("billing_cycle")
    private String billingCycle;

    private List<String> features;

    @JsonProperty("max_invoices")
    private Integer maxInvoices;

    @JsonProperty("max_users")
    private Integer maxUsers;

    @JsonProperty("is_popular")
    private Boolean isPopular = false;

    public SubscriptionPlanDTO() {
    }

    public SubscriptionPlanDTO(String id, String name, String description, BigDecimal monthlyPrice, BigDecimal yearlyPrice, String currency, String billingCycle, List<String> features, Integer maxInvoices, Integer maxUsers, Boolean isPopular) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.yearlyPrice = yearlyPrice;
        this.currency = currency;
        this.billingCycle = billingCycle;
        this.features = features;
        this.maxInvoices = maxInvoices;
        this.maxUsers = maxUsers;
        this.isPopular = isPopular;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public BigDecimal getYearlyPrice() {
        return yearlyPrice;
    }

    public void setYearlyPrice(BigDecimal yearlyPrice) {
        this.yearlyPrice = yearlyPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public Integer getMaxInvoices() {
        return maxInvoices;
    }

    public void setMaxInvoices(Integer maxInvoices) {
        this.maxInvoices = maxInvoices;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Boolean getIsPopular() {
        return isPopular;
    }

    public void setIsPopular(Boolean isPopular) {
        this.isPopular = isPopular;
    }
}
