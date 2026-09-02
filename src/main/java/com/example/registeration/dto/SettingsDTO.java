package com.example.registeration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettingsDTO {

    private OrganizationDTO organization;
    private InvoiceDTO invoices;
    private EstimateDTO estimates;
    private CreditNoteDTO creditNotes;
    private ProformaDTO proforma;
    private TermsDTO terms;
    private CustomerDTO customers;
    private PaymentDTO payments;
    private ExpenseDTO expenses;
    private TaxDTO tax;
    private PreferenceDTO preferences;

    public SettingsDTO() {
    }

    public OrganizationDTO getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDTO organization) {
        this.organization = organization;
    }

    public InvoiceDTO getInvoices() {
        return invoices;
    }

    public void setInvoices(InvoiceDTO invoices) {
        this.invoices = invoices;
    }

    public EstimateDTO getEstimates() {
        return estimates;
    }

    public void setEstimates(EstimateDTO estimates) {
        this.estimates = estimates;
    }

    public CreditNoteDTO getCreditNotes() {
        return creditNotes;
    }

    public void setCreditNotes(CreditNoteDTO creditNotes) {
        this.creditNotes = creditNotes;
    }

    public ProformaDTO getProforma() {
        return proforma;
    }

    public void setProforma(ProformaDTO proforma) {
        this.proforma = proforma;
    }

    public TermsDTO getTerms() {
        return terms;
    }

    public void setTerms(TermsDTO terms) {
        this.terms = terms;
    }

    public CustomerDTO getCustomers() {
        return customers;
    }

    public void setCustomers(CustomerDTO customers) {
        this.customers = customers;
    }

    public PaymentDTO getPayments() {
        return payments;
    }

    public void setPayments(PaymentDTO payments) {
        this.payments = payments;
    }

    public ExpenseDTO getExpenses() {
        return expenses;
    }

    public void setExpenses(ExpenseDTO expenses) {
        this.expenses = expenses;
    }

    public TaxDTO getTax() {
        return tax;
    }

    public void setTax(TaxDTO tax) {
        this.tax = tax;
    }

    public PreferenceDTO getPreferences() {
        return preferences;
    }

    public void setPreferences(PreferenceDTO preferences) {
        this.preferences = preferences;
    }

    // --- Section Sub-DTOs ---

    public static class OrganizationDTO {
        private String companyName;
        private String email;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String website;
        private String gstinOrg;
        private String companyLogo;

        public OrganizationDTO() {
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
    }

    public static class InvoiceDTO {
        private String invoicePrefix;
        private Boolean invoiceAutoNumber;
        private Boolean invoiceShowGstin;
        private String currency;
        private Number taxRate;
        private String invoiceDefaultTerms;
        private String invoiceDefaultNotes;
        private String invoiceTermsAndConditions;

        public InvoiceDTO() {
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

        public Number getTaxRate() {
            return taxRate;
        }

        public void setTaxRate(Number taxRate) {
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
    }

    public static class EstimateDTO {
        private String estimatePrefix;
        private Boolean estimateAutoNumber;
        private Integer estimateValidityDays;
        private String estimateDefaultNotes;
        private String estimateTermsAndConditions;

        public EstimateDTO() {
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
    }

    public static class CreditNoteDTO {
        private String creditNotePrefix;
        private Boolean creditNoteAutoNumber;
        private String creditNoteDefaultNotes;
        private String creditNoteTermsAndConditions;

        public CreditNoteDTO() {
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
    }

    public static class ProformaDTO {
        private String proformaPrefix;
        private Boolean proformaAutoNumber;
        private Integer proformaValidityDays;
        private String proformaDefaultNotes;
        private String proformaTermsAndConditions;

        public ProformaDTO() {
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
    }

    public static class TermsDTO {
        private String purchaseOrderTermsAndConditions;
        private String billTermsAndConditions;

        public TermsDTO() {
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
    }

    public static class CustomerDTO {
        private String customerDefaultType;
        private String customerDefaultState;
        private Boolean customerRequireEmail;
        private Boolean customerRequirePhone;

        public CustomerDTO() {
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
    }

    public static class PaymentDTO {
        private String paymentDefaultMethod;
        private String paymentModes;
        private Boolean paymentShowReceipt;

        public PaymentDTO() {
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
    }

    public static class ExpenseDTO {
        private String expenseDefaultCategory;
        private Boolean expenseBillableDefault;
        private String expenseCategories;

        public ExpenseDTO() {
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
    }

    public static class TaxDTO {
        private String gstRegistrationNo;
        private String panNumber;
        private String taxCalcMethod;
        private String defaultTaxSlab;

        public TaxDTO() {
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
    }

    public static class PreferenceDTO {
        private String dateFormat;
        private String numberFormat;
        private String theme;
        private String language;

        public PreferenceDTO() {
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
    }
}
