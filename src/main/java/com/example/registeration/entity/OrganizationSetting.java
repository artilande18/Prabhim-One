package com.example.registeration.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_settings")
public class OrganizationSetting {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;
    private UUID organizationId;

    // Organization Section
    private String companyName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String website;
    private String gstinOrg;

    @Column(columnDefinition = "TEXT")
    private String companyLogo;

    // Invoices Section
    private String invoicePrefix;
    private Boolean invoiceAutoNumber;
    private Boolean invoiceShowGstin;
    private String currency;
    private Double taxRate;
    private String invoiceDefaultTerms;

    @Column(columnDefinition = "TEXT")
    private String invoiceDefaultNotes;

    @Column(columnDefinition = "TEXT")
    private String invoiceTermsAndConditions;

    // Estimates Section
    private String estimatePrefix;
    private Boolean estimateAutoNumber;
    private Integer estimateValidityDays;

    @Column(columnDefinition = "TEXT")
    private String estimateDefaultNotes;

    @Column(columnDefinition = "TEXT")
    private String estimateTermsAndConditions;

    // Credit Notes Section
    private String creditNotePrefix;
    private Boolean creditNoteAutoNumber;

    @Column(columnDefinition = "TEXT")
    private String creditNoteDefaultNotes;

    @Column(columnDefinition = "TEXT")
    private String creditNoteTermsAndConditions;

    // Proforma Section
    private String proformaPrefix;
    private Boolean proformaAutoNumber;
    private Integer proformaValidityDays;

    @Column(columnDefinition = "TEXT")
    private String proformaDefaultNotes;

    @Column(columnDefinition = "TEXT")
    private String proformaTermsAndConditions;

    // Terms Section
    @Column(columnDefinition = "TEXT")
    private String purchaseOrderTermsAndConditions;

    @Column(columnDefinition = "TEXT")
    private String billTermsAndConditions;

    // Customers Section
    private String customerDefaultType;
    private String customerDefaultState;
    private Boolean customerRequireEmail;
    private Boolean customerRequirePhone;

    // Payments Section
    private String paymentDefaultMethod;
    private String paymentModes;
    private Boolean paymentShowReceipt;

    // Expenses Section
    private String expenseDefaultCategory;
    private Boolean expenseBillableDefault;

    @Column(columnDefinition = "TEXT")
    private String expenseCategories;

    // Tax Section
    private String gstRegistrationNo;
    private String panNumber;
    private String taxCalcMethod;
    private String defaultTaxSlab;

    // Preferences Section
    private String dateFormat;
    private String numberFormat;
    private String theme;
    private String language;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrganizationSetting() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getGstinOrg() {
        return gstinOrg;
    }

    public void setGstinOrg(String gstinOrg) {
        this.gstinOrg = gstinOrg;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public String getInvoicePrefix() {
        return invoicePrefix;
    }

    public void setInvoicePrefix(String invoicePrefix) {
        this.invoicePrefix = invoicePrefix;
    }

    public Boolean getInvoiceAutoNumber() {
        return invoiceAutoNumber;
    }

    public void setInvoiceAutoNumber(Boolean invoiceAutoNumber) {
        this.invoiceAutoNumber = invoiceAutoNumber;
    }

    public Boolean getInvoiceShowGstin() {
        return invoiceShowGstin;
    }

    public void setInvoiceShowGstin(Boolean invoiceShowGstin) {
        this.invoiceShowGstin = invoiceShowGstin;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }

    public String getInvoiceDefaultTerms() {
        return invoiceDefaultTerms;
    }

    public void setInvoiceDefaultTerms(String invoiceDefaultTerms) {
        this.invoiceDefaultTerms = invoiceDefaultTerms;
    }

    public String getInvoiceDefaultNotes() {
        return invoiceDefaultNotes;
    }

    public void setInvoiceDefaultNotes(String invoiceDefaultNotes) {
        this.invoiceDefaultNotes = invoiceDefaultNotes;
    }

    public String getInvoiceTermsAndConditions() {
        return invoiceTermsAndConditions;
    }

    public void setInvoiceTermsAndConditions(String invoiceTermsAndConditions) {
        this.invoiceTermsAndConditions = invoiceTermsAndConditions;
    }

    public String getEstimatePrefix() {
        return estimatePrefix;
    }

    public void setEstimatePrefix(String estimatePrefix) {
        this.estimatePrefix = estimatePrefix;
    }

    public Boolean getEstimateAutoNumber() {
        return estimateAutoNumber;
    }

    public void setEstimateAutoNumber(Boolean estimateAutoNumber) {
        this.estimateAutoNumber = estimateAutoNumber;
    }

    public Integer getEstimateValidityDays() {
        return estimateValidityDays;
    }

    public void setEstimateValidityDays(Integer estimateValidityDays) {
        this.estimateValidityDays = estimateValidityDays;
    }

    public String getEstimateDefaultNotes() {
        return estimateDefaultNotes;
    }

    public void setEstimateDefaultNotes(String estimateDefaultNotes) {
        this.estimateDefaultNotes = estimateDefaultNotes;
    }

    public String getEstimateTermsAndConditions() {
        return estimateTermsAndConditions;
    }

    public void setEstimateTermsAndConditions(String estimateTermsAndConditions) {
        this.estimateTermsAndConditions = estimateTermsAndConditions;
    }

    public String getCreditNotePrefix() {
        return creditNotePrefix;
    }

    public void setCreditNotePrefix(String creditNotePrefix) {
        this.creditNotePrefix = creditNotePrefix;
    }

    public Boolean getCreditNoteAutoNumber() {
        return creditNoteAutoNumber;
    }

    public void setCreditNoteAutoNumber(Boolean creditNoteAutoNumber) {
        this.creditNoteAutoNumber = creditNoteAutoNumber;
    }

    public String getCreditNoteDefaultNotes() {
        return creditNoteDefaultNotes;
    }

    public void setCreditNoteDefaultNotes(String creditNoteDefaultNotes) {
        this.creditNoteDefaultNotes = creditNoteDefaultNotes;
    }

    public String getCreditNoteTermsAndConditions() {
        return creditNoteTermsAndConditions;
    }

    public void setCreditNoteTermsAndConditions(String creditNoteTermsAndConditions) {
        this.creditNoteTermsAndConditions = creditNoteTermsAndConditions;
    }

    public String getProformaPrefix() {
        return proformaPrefix;
    }

    public void setProformaPrefix(String proformaPrefix) {
        this.proformaPrefix = proformaPrefix;
    }

    public Boolean getProformaAutoNumber() {
        return proformaAutoNumber;
    }

    public void setProformaAutoNumber(Boolean proformaAutoNumber) {
        this.proformaAutoNumber = proformaAutoNumber;
    }

    public Integer getProformaValidityDays() {
        return proformaValidityDays;
    }

    public void setProformaValidityDays(Integer proformaValidityDays) {
        this.proformaValidityDays = proformaValidityDays;
    }

    public String getProformaDefaultNotes() {
        return proformaDefaultNotes;
    }

    public void setProformaDefaultNotes(String proformaDefaultNotes) {
        this.proformaDefaultNotes = proformaDefaultNotes;
    }

    public String getProformaTermsAndConditions() {
        return proformaTermsAndConditions;
    }

    public void setProformaTermsAndConditions(String proformaTermsAndConditions) {
        this.proformaTermsAndConditions = proformaTermsAndConditions;
    }

    public String getPurchaseOrderTermsAndConditions() {
        return purchaseOrderTermsAndConditions;
    }

    public void setPurchaseOrderTermsAndConditions(String purchaseOrderTermsAndConditions) {
        this.purchaseOrderTermsAndConditions = purchaseOrderTermsAndConditions;
    }

    public String getBillTermsAndConditions() {
        return billTermsAndConditions;
    }

    public void setBillTermsAndConditions(String billTermsAndConditions) {
        this.billTermsAndConditions = billTermsAndConditions;
    }

    public String getCustomerDefaultType() {
        return customerDefaultType;
    }

    public void setCustomerDefaultType(String customerDefaultType) {
        this.customerDefaultType = customerDefaultType;
    }

    public String getCustomerDefaultState() {
        return customerDefaultState;
    }

    public void setCustomerDefaultState(String customerDefaultState) {
        this.customerDefaultState = customerDefaultState;
    }

    public Boolean getCustomerRequireEmail() {
        return customerRequireEmail;
    }

    public void setCustomerRequireEmail(Boolean customerRequireEmail) {
        this.customerRequireEmail = customerRequireEmail;
    }

    public Boolean getCustomerRequirePhone() {
        return customerRequirePhone;
    }

    public void setCustomerRequirePhone(Boolean customerRequirePhone) {
        this.customerRequirePhone = customerRequirePhone;
    }

    public String getPaymentDefaultMethod() {
        return paymentDefaultMethod;
    }

    public void setPaymentDefaultMethod(String paymentDefaultMethod) {
        this.paymentDefaultMethod = paymentDefaultMethod;
    }

    public String getPaymentModes() {
        return paymentModes;
    }

    public void setPaymentModes(String paymentModes) {
        this.paymentModes = paymentModes;
    }

    public Boolean getPaymentShowReceipt() {
        return paymentShowReceipt;
    }

    public void setPaymentShowReceipt(Boolean paymentShowReceipt) {
        this.paymentShowReceipt = paymentShowReceipt;
    }

    public String getExpenseDefaultCategory() {
        return expenseDefaultCategory;
    }

    public void setExpenseDefaultCategory(String expenseDefaultCategory) {
        this.expenseDefaultCategory = expenseDefaultCategory;
    }

    public Boolean getExpenseBillableDefault() {
        return expenseBillableDefault;
    }

    public void setExpenseBillableDefault(Boolean expenseBillableDefault) {
        this.expenseBillableDefault = expenseBillableDefault;
    }

    public String getExpenseCategories() {
        return expenseCategories;
    }

    public void setExpenseCategories(String expenseCategories) {
        this.expenseCategories = expenseCategories;
    }

    public String getGstRegistrationNo() {
        return gstRegistrationNo;
    }

    public void setGstRegistrationNo(String gstRegistrationNo) {
        this.gstRegistrationNo = gstRegistrationNo;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getTaxCalcMethod() {
        return taxCalcMethod;
    }

    public void setTaxCalcMethod(String taxCalcMethod) {
        this.taxCalcMethod = taxCalcMethod;
    }

    public String getDefaultTaxSlab() {
        return defaultTaxSlab;
    }

    public void setDefaultTaxSlab(String defaultTaxSlab) {
        this.defaultTaxSlab = defaultTaxSlab;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
