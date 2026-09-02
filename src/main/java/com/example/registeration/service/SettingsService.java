package com.example.registeration.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.registeration.dto.SettingsDTO;
import com.example.registeration.dto.SettingsSaveResponse;
import com.example.registeration.entity.OrganizationSetting;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.exception.SettingsValidationException;
import com.example.registeration.repository.OrganizationSettingRepository;
import com.example.registeration.repository.UserRepository;

@Service
public class SettingsService {

    private final OrganizationSettingRepository organizationSettingRepository;
    private final UserRepository userRepository;

    public SettingsService(OrganizationSettingRepository organizationSettingRepository, UserRepository userRepository) {
        this.organizationSettingRepository = organizationSettingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SettingsDTO getSettings(UUID userId) {
        validateUserExists(userId);

        OrganizationSetting setting = organizationSettingRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettingEntity(userId));

        return mapToDTO(setting);
    }

    @Transactional
    public SettingsSaveResponse updateSettings(UUID userId, SettingsDTO request) {
        validateUserExists(userId);
        validateSettingsRequest(request);

        OrganizationSetting setting = organizationSettingRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettingEntity(userId));

        applyUpdates(setting, request);
        setting.setUpdatedAt(LocalDateTime.now());
        organizationSettingRepository.save(setting);

        String isoUtcTimestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        return new SettingsSaveResponse("Settings saved successfully.", isoUtcTimestamp);
    }

    private void validateUserExists(UUID userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private void validateSettingsRequest(SettingsDTO request) {
        if (request == null) {
            return;
        }

        Map<String, String> details = new LinkedHashMap<>();

        if (request.getOrganization() != null) {
            SettingsDTO.OrganizationDTO org = request.getOrganization();

            if (org.getEmail() != null && !org.getEmail().trim().isEmpty()) {
                String email = org.getEmail().trim();
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    details.put("email", "Must be a valid email address.");
                }
            }

            if (org.getPhone() != null && !org.getPhone().trim().isEmpty()) {
                String rawPhone = org.getPhone().trim();
                String digitsOnly = rawPhone.replaceAll("[^0-9]", "");
                boolean isValidPhone = false;

                if (digitsOnly.length() == 10) {
                    isValidPhone = true;
                } else if (digitsOnly.length() == 12 && digitsOnly.startsWith("91")) {
                    isValidPhone = true;
                } else if (digitsOnly.length() == 11 && digitsOnly.startsWith("0")) {
                    isValidPhone = true;
                }

                if (!isValidPhone) {
                    details.put("phone", "Must be a valid 10-digit phone number.");
                }
            }
        }

        if (!details.isEmpty()) {
            throw new SettingsValidationException(details);
        }
    }

    private OrganizationSetting createDefaultSettingEntity(UUID userId) {
        OrganizationSetting s = new OrganizationSetting();
        s.setUserId(userId);
        s.setOrganizationId(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));

        // Organization
        s.setCompanyName("Prabhim Technologies (OPC) Pvt. Ltd.");
        s.setEmail("info@prabhimtechnologies.in");
        s.setPhone("+91-9403301412");
        s.setAddress("FL-1002 A, Utsav Residency Phase 1, Awhalwadi Road,");
        s.setCity("Wagholi, Pune, 412202, India");
        s.setState("Maharashtra");
        s.setWebsite("");
        s.setGstinOrg("27AAPCP6019G1Z5");
        s.setCompanyLogo("");

        // Invoices
        s.setInvoicePrefix("INV-");
        s.setInvoiceAutoNumber(true);
        s.setInvoiceShowGstin(true);
        s.setCurrency("₹");
        s.setTaxRate(18.0);
        s.setInvoiceDefaultTerms("Due on Receipt");
        s.setInvoiceDefaultNotes("Thank you for doing business with us!");
        s.setInvoiceTermsAndConditions("1. Goods once sold will not be taken back...");

        // Estimates
        s.setEstimatePrefix("EST-");
        s.setEstimateAutoNumber(true);
        s.setEstimateValidityDays(30);
        s.setEstimateDefaultNotes("");
        s.setEstimateTermsAndConditions("1. This estimate is valid for 30 days...");

        // Credit Notes
        s.setCreditNotePrefix("CN-");
        s.setCreditNoteAutoNumber(true);
        s.setCreditNoteDefaultNotes("");
        s.setCreditNoteTermsAndConditions("1. Credit balance must be applied...");

        // Proforma
        s.setProformaPrefix("PI-");
        s.setProformaAutoNumber(true);
        s.setProformaValidityDays(30);
        s.setProformaDefaultNotes("");
        s.setProformaTermsAndConditions("1. This proforma invoice is sent for approval...");

        // Terms
        s.setPurchaseOrderTermsAndConditions("1. Goods received subject to inspection...");
        s.setBillTermsAndConditions("1. Bill is payable within the agreed credit period...");

        // Customers
        s.setCustomerDefaultType("Existing");
        s.setCustomerDefaultState("Maharashtra");
        s.setCustomerRequireEmail(false);
        s.setCustomerRequirePhone(true);

        // Payments
        s.setPaymentDefaultMethod("UPI");
        s.setPaymentModes("UPI,Cash,Bank Transfer,Cheque,Credit Card,Debit Card");
        s.setPaymentShowReceipt(true);

        // Expenses
        s.setExpenseDefaultCategory("Office Supplies");
        s.setExpenseBillableDefault(false);
        s.setExpenseCategories("Rent & Accommodation,Office Supplies,Travel Expenses,Utilities,Miscellaneous");

        // Tax
        s.setGstRegistrationNo("27AAPCP6019G1Z5");
        s.setPanNumber("ABCDE1234F");
        s.setTaxCalcMethod("Exclusive");
        s.setDefaultTaxSlab("18");

        // Preferences
        s.setDateFormat("YYYY-MM-DD");
        s.setNumberFormat("Indian");
        s.setTheme("light");
        s.setLanguage("English");

        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());

        return organizationSettingRepository.save(s);
    }

    private void applyUpdates(OrganizationSetting s, SettingsDTO req) {
        if (req == null) return;

        if (req.getOrganization() != null) {
            SettingsDTO.OrganizationDTO org = req.getOrganization();
            if (org.getCompanyName() != null) s.setCompanyName(org.getCompanyName());
            if (org.getEmail() != null) s.setEmail(org.getEmail());
            if (org.getPhone() != null) s.setPhone(org.getPhone());
            if (org.getAddress() != null) s.setAddress(org.getAddress());
            if (org.getCity() != null) s.setCity(org.getCity());
            if (org.getState() != null) s.setState(org.getState());
            if (org.getWebsite() != null) s.setWebsite(org.getWebsite());
            if (org.getGstinOrg() != null) s.setGstinOrg(org.getGstinOrg());
            if (org.getCompanyLogo() != null) s.setCompanyLogo(org.getCompanyLogo());
        }

        if (req.getInvoices() != null) {
            SettingsDTO.InvoiceDTO inv = req.getInvoices();
            if (inv.getInvoicePrefix() != null) s.setInvoicePrefix(inv.getInvoicePrefix());
            if (inv.getInvoiceAutoNumber() != null) s.setInvoiceAutoNumber(inv.getInvoiceAutoNumber());
            if (inv.getInvoiceShowGstin() != null) s.setInvoiceShowGstin(inv.getInvoiceShowGstin());
            if (inv.getCurrency() != null) s.setCurrency(inv.getCurrency());
            if (inv.getTaxRate() != null) s.setTaxRate(inv.getTaxRate().doubleValue());
            if (inv.getInvoiceDefaultTerms() != null) s.setInvoiceDefaultTerms(inv.getInvoiceDefaultTerms());
            if (inv.getInvoiceDefaultNotes() != null) s.setInvoiceDefaultNotes(inv.getInvoiceDefaultNotes());
            if (inv.getInvoiceTermsAndConditions() != null) s.setInvoiceTermsAndConditions(inv.getInvoiceTermsAndConditions());
        }

        if (req.getEstimates() != null) {
            SettingsDTO.EstimateDTO est = req.getEstimates();
            if (est.getEstimatePrefix() != null) s.setEstimatePrefix(est.getEstimatePrefix());
            if (est.getEstimateAutoNumber() != null) s.setEstimateAutoNumber(est.getEstimateAutoNumber());
            if (est.getEstimateValidityDays() != null) s.setEstimateValidityDays(est.getEstimateValidityDays());
            if (est.getEstimateDefaultNotes() != null) s.setEstimateDefaultNotes(est.getEstimateDefaultNotes());
            if (est.getEstimateTermsAndConditions() != null) s.setEstimateTermsAndConditions(est.getEstimateTermsAndConditions());
        }

        if (req.getCreditNotes() != null) {
            SettingsDTO.CreditNoteDTO cn = req.getCreditNotes();
            if (cn.getCreditNotePrefix() != null) s.setCreditNotePrefix(cn.getCreditNotePrefix());
            if (cn.getCreditNoteAutoNumber() != null) s.setCreditNoteAutoNumber(cn.getCreditNoteAutoNumber());
            if (cn.getCreditNoteDefaultNotes() != null) s.setCreditNoteDefaultNotes(cn.getCreditNoteDefaultNotes());
            if (cn.getCreditNoteTermsAndConditions() != null) s.setCreditNoteTermsAndConditions(cn.getCreditNoteTermsAndConditions());
        }

        if (req.getProforma() != null) {
            SettingsDTO.ProformaDTO prof = req.getProforma();
            if (prof.getProformaPrefix() != null) s.setProformaPrefix(prof.getProformaPrefix());
            if (prof.getProformaAutoNumber() != null) s.setProformaAutoNumber(prof.getProformaAutoNumber());
            if (prof.getProformaValidityDays() != null) s.setProformaValidityDays(prof.getProformaValidityDays());
            if (prof.getProformaDefaultNotes() != null) s.setProformaDefaultNotes(prof.getProformaDefaultNotes());
            if (prof.getProformaTermsAndConditions() != null) s.setProformaTermsAndConditions(prof.getProformaTermsAndConditions());
        }

        if (req.getTerms() != null) {
            SettingsDTO.TermsDTO trm = req.getTerms();
            if (trm.getPurchaseOrderTermsAndConditions() != null) s.setPurchaseOrderTermsAndConditions(trm.getPurchaseOrderTermsAndConditions());
            if (trm.getBillTermsAndConditions() != null) s.setBillTermsAndConditions(trm.getBillTermsAndConditions());
        }

        if (req.getCustomers() != null) {
            SettingsDTO.CustomerDTO cust = req.getCustomers();
            if (cust.getCustomerDefaultType() != null) s.setCustomerDefaultType(cust.getCustomerDefaultType());
            if (cust.getCustomerDefaultState() != null) s.setCustomerDefaultState(cust.getCustomerDefaultState());
            if (cust.getCustomerRequireEmail() != null) s.setCustomerRequireEmail(cust.getCustomerRequireEmail());
            if (cust.getCustomerRequirePhone() != null) s.setCustomerRequirePhone(cust.getCustomerRequirePhone());
        }

        if (req.getPayments() != null) {
            SettingsDTO.PaymentDTO pay = req.getPayments();
            if (pay.getPaymentDefaultMethod() != null) s.setPaymentDefaultMethod(pay.getPaymentDefaultMethod());
            if (pay.getPaymentModes() != null) s.setPaymentModes(pay.getPaymentModes());
            if (pay.getPaymentShowReceipt() != null) s.setPaymentShowReceipt(pay.getPaymentShowReceipt());
        }

        if (req.getExpenses() != null) {
            SettingsDTO.ExpenseDTO exp = req.getExpenses();
            if (exp.getExpenseDefaultCategory() != null) s.setExpenseDefaultCategory(exp.getExpenseDefaultCategory());
            if (exp.getExpenseBillableDefault() != null) s.setExpenseBillableDefault(exp.getExpenseBillableDefault());
            if (exp.getExpenseCategories() != null) s.setExpenseCategories(exp.getExpenseCategories());
        }

        if (req.getTax() != null) {
            SettingsDTO.TaxDTO tx = req.getTax();
            if (tx.getGstRegistrationNo() != null) s.setGstRegistrationNo(tx.getGstRegistrationNo());
            if (tx.getPanNumber() != null) s.setPanNumber(tx.getPanNumber());
            if (tx.getTaxCalcMethod() != null) s.setTaxCalcMethod(tx.getTaxCalcMethod());
            if (tx.getDefaultTaxSlab() != null) s.setDefaultTaxSlab(tx.getDefaultTaxSlab());
        }

        if (req.getPreferences() != null) {
            SettingsDTO.PreferenceDTO pref = req.getPreferences();
            if (pref.getDateFormat() != null) s.setDateFormat(pref.getDateFormat());
            if (pref.getNumberFormat() != null) s.setNumberFormat(pref.getNumberFormat());
            if (pref.getTheme() != null) s.setTheme(pref.getTheme());
            if (pref.getLanguage() != null) s.setLanguage(pref.getLanguage());
        }
    }

    private SettingsDTO mapToDTO(OrganizationSetting s) {
        SettingsDTO dto = new SettingsDTO();

        // Organization
        SettingsDTO.OrganizationDTO org = new SettingsDTO.OrganizationDTO();
        org.setCompanyName(s.getCompanyName());
        org.setEmail(s.getEmail());
        org.setPhone(s.getPhone());
        org.setAddress(s.getAddress());
        org.setCity(s.getCity());
        org.setState(s.getState());
        org.setWebsite(s.getWebsite());
        org.setGstinOrg(s.getGstinOrg());
        org.setCompanyLogo(s.getCompanyLogo());
        dto.setOrganization(org);

        // Invoices
        SettingsDTO.InvoiceDTO inv = new SettingsDTO.InvoiceDTO();
        inv.setInvoicePrefix(s.getInvoicePrefix());
        inv.setInvoiceAutoNumber(s.getInvoiceAutoNumber());
        inv.setInvoiceShowGstin(s.getInvoiceShowGstin());
        inv.setCurrency(s.getCurrency());
        if (s.getTaxRate() != null) {
            if (s.getTaxRate() == Math.floor(s.getTaxRate())) {
                inv.setTaxRate(s.getTaxRate().intValue());
            } else {
                inv.setTaxRate(s.getTaxRate());
            }
        }
        inv.setInvoiceDefaultTerms(s.getInvoiceDefaultTerms());
        inv.setInvoiceDefaultNotes(s.getInvoiceDefaultNotes());
        inv.setInvoiceTermsAndConditions(s.getInvoiceTermsAndConditions());
        dto.setInvoices(inv);

        // Estimates
        SettingsDTO.EstimateDTO est = new SettingsDTO.EstimateDTO();
        est.setEstimatePrefix(s.getEstimatePrefix());
        est.setEstimateAutoNumber(s.getEstimateAutoNumber());
        est.setEstimateValidityDays(s.getEstimateValidityDays());
        est.setEstimateDefaultNotes(s.getEstimateDefaultNotes());
        est.setEstimateTermsAndConditions(s.getEstimateTermsAndConditions());
        dto.setEstimates(est);

        // Credit Notes
        SettingsDTO.CreditNoteDTO cn = new SettingsDTO.CreditNoteDTO();
        cn.setCreditNotePrefix(s.getCreditNotePrefix());
        cn.setCreditNoteAutoNumber(s.getCreditNoteAutoNumber());
        cn.setCreditNoteDefaultNotes(s.getCreditNoteDefaultNotes());
        cn.setCreditNoteTermsAndConditions(s.getCreditNoteTermsAndConditions());
        dto.setCreditNotes(cn);

        // Proforma
        SettingsDTO.ProformaDTO prof = new SettingsDTO.ProformaDTO();
        prof.setProformaPrefix(s.getProformaPrefix());
        prof.setProformaAutoNumber(s.getProformaAutoNumber());
        prof.setProformaValidityDays(s.getProformaValidityDays());
        prof.setProformaDefaultNotes(s.getProformaDefaultNotes());
        prof.setProformaTermsAndConditions(s.getProformaTermsAndConditions());
        dto.setProforma(prof);

        // Terms
        SettingsDTO.TermsDTO trm = new SettingsDTO.TermsDTO();
        trm.setPurchaseOrderTermsAndConditions(s.getPurchaseOrderTermsAndConditions());
        trm.setBillTermsAndConditions(s.getBillTermsAndConditions());
        dto.setTerms(trm);

        // Customers
        SettingsDTO.CustomerDTO cust = new SettingsDTO.CustomerDTO();
        cust.setCustomerDefaultType(s.getCustomerDefaultType());
        cust.setCustomerDefaultState(s.getCustomerDefaultState());
        cust.setCustomerRequireEmail(s.getCustomerRequireEmail());
        cust.setCustomerRequirePhone(s.getCustomerRequirePhone());
        dto.setCustomers(cust);

        // Payments
        SettingsDTO.PaymentDTO pay = new SettingsDTO.PaymentDTO();
        pay.setPaymentDefaultMethod(s.getPaymentDefaultMethod());
        pay.setPaymentModes(s.getPaymentModes());
        pay.setPaymentShowReceipt(s.getPaymentShowReceipt());
        dto.setPayments(pay);

        // Expenses
        SettingsDTO.ExpenseDTO exp = new SettingsDTO.ExpenseDTO();
        exp.setExpenseDefaultCategory(s.getExpenseDefaultCategory());
        exp.setExpenseBillableDefault(s.getExpenseBillableDefault());
        exp.setExpenseCategories(s.getExpenseCategories());
        dto.setExpenses(exp);

        // Tax
        SettingsDTO.TaxDTO tx = new SettingsDTO.TaxDTO();
        tx.setGstRegistrationNo(s.getGstRegistrationNo());
        tx.setPanNumber(s.getPanNumber());
        tx.setTaxCalcMethod(s.getTaxCalcMethod());
        tx.setDefaultTaxSlab(s.getDefaultTaxSlab());
        dto.setTax(tx);

        // Preferences
        SettingsDTO.PreferenceDTO pref = new SettingsDTO.PreferenceDTO();
        pref.setDateFormat(s.getDateFormat());
        pref.setNumberFormat(s.getNumberFormat());
        pref.setTheme(s.getTheme());
        pref.setLanguage(s.getLanguage());
        dto.setPreferences(pref);

        return dto;
    }
}
