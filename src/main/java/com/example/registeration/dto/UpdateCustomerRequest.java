package com.example.registeration.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateCustomerRequest {

    @JsonProperty("customer_type")
    private String customerType;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;

    @JsonProperty("primary_phone")
    private String primaryPhone;

    @JsonProperty("secondary_phone")
    private String secondaryPhone;

    private String website;

    @JsonProperty("billing_attention")
    private String billingAttention;

    @JsonProperty("billing_address_line_1")
    private String billingAddressLine1;

    @JsonProperty("billing_address_line_2")
    private String billingAddressLine2;

    @JsonProperty("billing_city")
    private String billingCity;

    @JsonProperty("billing_state")
    private String billingState;

    @JsonProperty("billing_country")
    private String billingCountry;

    @JsonProperty("billing_postal_code")
    private String billingPostalCode;

    @JsonProperty("shipping_same_as_billing")
    private Boolean shippingSameAsBilling;

    @JsonProperty("shipping_attention")
    private String shippingAttention;

    @JsonProperty("shipping_address_line_1")
    private String shippingAddressLine1;

    @JsonProperty("shipping_address_line_2")
    private String shippingAddressLine2;

    @JsonProperty("shipping_city")
    private String shippingCity;

    @JsonProperty("shipping_state")
    private String shippingState;

    @JsonProperty("shipping_country")
    private String shippingCountry;

    @JsonProperty("shipping_postal_code")
    private String shippingPostalCode;

    @JsonProperty("gst_number")
    private String gstNumber;

    @JsonProperty("pan_number")
    private String panNumber;

    @JsonProperty("tax_preference")
    private String taxPreference;

    private String currency;

    @JsonProperty("payment_terms")
    private String paymentTerms;

    @JsonProperty("credit_limit")
    private BigDecimal creditLimit;

    private String notes;

    public UpdateCustomerRequest() {
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getBillingAttention() {
        return billingAttention;
    }

    public void setBillingAttention(String billingAttention) {
        this.billingAttention = billingAttention;
    }

    public String getBillingAddressLine1() {
        return billingAddressLine1;
    }

    public void setBillingAddressLine1(String billingAddressLine1) {
        this.billingAddressLine1 = billingAddressLine1;
    }

    public String getBillingAddressLine2() {
        return billingAddressLine2;
    }

    public void setBillingAddressLine2(String billingAddressLine2) {
        this.billingAddressLine2 = billingAddressLine2;
    }

    public String getBillingCity() {
        return billingCity;
    }

    public void setBillingCity(String billingCity) {
        this.billingCity = billingCity;
    }

    public String getBillingState() {
        return billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getBillingCountry() {
        return billingCountry;
    }

    public void setBillingCountry(String billingCountry) {
        this.billingCountry = billingCountry;
    }

    public String getBillingPostalCode() {
        return billingPostalCode;
    }

    public void setBillingPostalCode(String billingPostalCode) {
        this.billingPostalCode = billingPostalCode;
    }

    public Boolean getShippingSameAsBilling() {
        return shippingSameAsBilling;
    }

    public void setShippingSameAsBilling(Boolean shippingSameAsBilling) {
        this.shippingSameAsBilling = shippingSameAsBilling;
    }

    public String getShippingAttention() {
        return shippingAttention;
    }

    public void setShippingAttention(String shippingAttention) {
        this.shippingAttention = shippingAttention;
    }

    public String getShippingAddressLine1() {
        return shippingAddressLine1;
    }

    public void setShippingAddressLine1(String shippingAddressLine1) {
        this.shippingAddressLine1 = shippingAddressLine1;
    }

    public String getShippingAddressLine2() {
        return shippingAddressLine2;
    }

    public void setShippingAddressLine2(String shippingAddressLine2) {
        this.shippingAddressLine2 = shippingAddressLine2;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }

    public String getShippingState() {
        return shippingState;
    }

    public void setShippingState(String shippingState) {
        this.shippingState = shippingState;
    }

    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }

    public String getShippingPostalCode() {
        return shippingPostalCode;
    }

    public void setShippingPostalCode(String shippingPostalCode) {
        this.shippingPostalCode = shippingPostalCode;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getTaxPreference() {
        return taxPreference;
    }

    public void setTaxPreference(String taxPreference) {
        this.taxPreference = taxPreference;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
