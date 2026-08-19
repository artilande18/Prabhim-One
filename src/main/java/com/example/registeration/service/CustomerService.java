package com.example.registeration.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.registeration.dto.CreateCustomerRequest;
import com.example.registeration.dto.UpdateCustomerRequest;
import com.example.registeration.dto.CustomerResponse;
import com.example.registeration.entity.Customer;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.CustomerRepository;
import com.example.registeration.repository.UserRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request, UUID createdByUserId) {
        // Validate createdBy user exists
        if (!userRepository.existsById(createdByUserId)) {
            throw new ResourceNotFoundException("User not found with id: " + createdByUserId);
        }

        Customer customer = new Customer();
        customer.setCustomerType(request.getCustomerType());
        customer.setDisplayName(request.getDisplayName());
        customer.setCompanyName(request.getCompanyName());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPrimaryPhone(request.getPrimaryPhone());
        customer.setSecondaryPhone(request.getSecondaryPhone());
        customer.setWebsite(request.getWebsite());

        // Billing info
        customer.setBillingAttention(request.getBillingAttention());
        customer.setBillingAddressLine1(request.getBillingAddressLine1());
        customer.setBillingAddressLine2(request.getBillingAddressLine2());
        customer.setBillingCity(request.getBillingCity());
        customer.setBillingState(request.getBillingState());
        customer.setBillingCountry(request.getBillingCountry());
        customer.setBillingPostalCode(request.getBillingPostalCode());

        // Shipping info mapping based on shippingSameAsBilling flag
        boolean sameAsBilling = request.getShippingSameAsBilling() != null && request.getShippingSameAsBilling();
        customer.setShippingSameAsBilling(sameAsBilling);

        if (sameAsBilling) {
            customer.setShippingAttention(request.getBillingAttention());
            customer.setShippingAddressLine1(request.getBillingAddressLine1());
            customer.setShippingAddressLine2(request.getBillingAddressLine2());
            customer.setShippingCity(request.getBillingCity());
            customer.setShippingState(request.getBillingState());
            customer.setShippingCountry(request.getBillingCountry());
            customer.setShippingPostalCode(request.getBillingPostalCode());
        } else {
            customer.setShippingAttention(request.getShippingAttention());
            customer.setShippingAddressLine1(request.getShippingAddressLine1());
            customer.setShippingAddressLine2(request.getShippingAddressLine2());
            customer.setShippingCity(request.getShippingCity());
            customer.setShippingState(request.getShippingState());
            customer.setShippingCountry(request.getShippingCountry());
            customer.setShippingPostalCode(request.getShippingPostalCode());
        }

        customer.setGstNumber(request.getGstNumber());
        customer.setPanNumber(request.getPanNumber());
        customer.setTaxPreference(request.getTaxPreference());
        customer.setCurrency(request.getCurrency());
        customer.setPaymentTerms(request.getPaymentTerms());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setNotes(request.getNotes());

        // System/Audit fields
        customer.setOrganization(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")); // default org UUID
        customer.setCreatedBy(createdByUserId);
        customer.setStatus("active");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setIsDeleted(false);
        customer.setDeletedAt(null);

        Customer savedCustomer = customerRepository.save(customer);
        return mapToCustomerResponse(savedCustomer);
    }

    public CustomerResponse getCustomer(UUID id, UUID userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (customer.getIsDeleted() != null && customer.getIsDeleted()) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }

        return mapToCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request, boolean isPartial, UUID userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (customer.getIsDeleted() != null && customer.getIsDeleted()) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }

        // Basic Info
        if (!isPartial || request.getCustomerType() != null) customer.setCustomerType(request.getCustomerType());
        if (!isPartial || request.getDisplayName() != null) customer.setDisplayName(request.getDisplayName());
        if (!isPartial || request.getCompanyName() != null) customer.setCompanyName(request.getCompanyName());
        if (!isPartial || request.getFirstName() != null) customer.setFirstName(request.getFirstName());
        if (!isPartial || request.getLastName() != null) customer.setLastName(request.getLastName());
        if (!isPartial || request.getEmail() != null) customer.setEmail(request.getEmail());
        if (!isPartial || request.getPrimaryPhone() != null) customer.setPrimaryPhone(request.getPrimaryPhone());
        if (!isPartial || request.getSecondaryPhone() != null) customer.setSecondaryPhone(request.getSecondaryPhone());
        if (!isPartial || request.getWebsite() != null) customer.setWebsite(request.getWebsite());

        // Billing Info
        if (!isPartial || request.getBillingAttention() != null) customer.setBillingAttention(request.getBillingAttention());
        if (!isPartial || request.getBillingAddressLine1() != null) customer.setBillingAddressLine1(request.getBillingAddressLine1());
        if (!isPartial || request.getBillingAddressLine2() != null) customer.setBillingAddressLine2(request.getBillingAddressLine2());
        if (!isPartial || request.getBillingCity() != null) customer.setBillingCity(request.getBillingCity());
        if (!isPartial || request.getBillingState() != null) customer.setBillingState(request.getBillingState());
        if (!isPartial || request.getBillingCountry() != null) customer.setBillingCountry(request.getBillingCountry());
        if (!isPartial || request.getBillingPostalCode() != null) customer.setBillingPostalCode(request.getBillingPostalCode());

        // Same as Billing Flag
        if (!isPartial || request.getShippingSameAsBilling() != null) {
            customer.setShippingSameAsBilling(request.getShippingSameAsBilling() != null && request.getShippingSameAsBilling());
        }

        boolean sameAsBilling = customer.getShippingSameAsBilling() != null && customer.getShippingSameAsBilling();

        if (sameAsBilling) {
            customer.setShippingAttention(customer.getBillingAttention());
            customer.setShippingAddressLine1(customer.getBillingAddressLine1());
            customer.setShippingAddressLine2(customer.getBillingAddressLine2());
            customer.setShippingCity(customer.getBillingCity());
            customer.setShippingState(customer.getBillingState());
            customer.setShippingCountry(customer.getBillingCountry());
            customer.setShippingPostalCode(customer.getBillingPostalCode());
        } else {
            if (!isPartial || request.getShippingAttention() != null) customer.setShippingAttention(request.getShippingAttention());
            if (!isPartial || request.getShippingAddressLine1() != null) customer.setShippingAddressLine1(request.getShippingAddressLine1());
            if (!isPartial || request.getShippingAddressLine2() != null) customer.setShippingAddressLine2(request.getShippingAddressLine2());
            if (!isPartial || request.getShippingCity() != null) customer.setShippingCity(request.getShippingCity());
            if (!isPartial || request.getShippingState() != null) customer.setShippingState(request.getShippingState());
            if (!isPartial || request.getShippingCountry() != null) customer.setShippingCountry(request.getShippingCountry());
            if (!isPartial || request.getShippingPostalCode() != null) customer.setShippingPostalCode(request.getShippingPostalCode());
        }

        // Tax & Preference Info
        if (!isPartial || request.getGstNumber() != null) customer.setGstNumber(request.getGstNumber());
        if (!isPartial || request.getPanNumber() != null) customer.setPanNumber(request.getPanNumber());
        if (!isPartial || request.getTaxPreference() != null) customer.setTaxPreference(request.getTaxPreference());
        if (!isPartial || request.getCurrency() != null) customer.setCurrency(request.getCurrency());
        if (!isPartial || request.getPaymentTerms() != null) customer.setPaymentTerms(request.getPaymentTerms());
        if (!isPartial || request.getCreditLimit() != null) customer.setCreditLimit(request.getCreditLimit());
        if (!isPartial || request.getNotes() != null) customer.setNotes(request.getNotes());

        customer.setUpdatedAt(LocalDateTime.now());
        Customer savedCustomer = customerRepository.save(customer);
        return mapToCustomerResponse(savedCustomer);
    }

    @Transactional
    public void deleteCustomer(UUID id, UUID userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (customer.getIsDeleted() != null && customer.getIsDeleted()) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }

        customer.setIsDeleted(true);
        customer.setDeletedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    @Transactional
    public CustomerResponse restoreCustomer(UUID id, UUID userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setIsDeleted(false);
        customer.setDeletedAt(null);
        customer.setUpdatedAt(LocalDateTime.now());

        Customer savedCustomer = customerRepository.save(customer);
        return mapToCustomerResponse(savedCustomer);
    }

    private CustomerResponse mapToCustomerResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setOrganization(customer.getOrganization());
        response.setCustomerType(customer.getCustomerType());
        response.setDisplayName(customer.getDisplayName());
        response.setCompanyName(customer.getCompanyName());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPrimaryPhone(customer.getPrimaryPhone());
        response.setSecondaryPhone(customer.getSecondaryPhone());
        response.setWebsite(customer.getWebsite());

        response.setBillingAttention(customer.getBillingAttention());
        response.setBillingAddressLine1(customer.getBillingAddressLine1());
        response.setBillingAddressLine2(customer.getBillingAddressLine2());
        response.setBillingCity(customer.getBillingCity());
        response.setBillingState(customer.getBillingState());
        response.setBillingCountry(customer.getBillingCountry());
        response.setBillingPostalCode(customer.getBillingPostalCode());

        response.setShippingSameAsBilling(customer.getShippingSameAsBilling());
        response.setShippingAttention(customer.getShippingAttention());
        response.setShippingAddressLine1(customer.getShippingAddressLine1());
        response.setShippingAddressLine2(customer.getShippingAddressLine2());
        response.setShippingCity(customer.getShippingCity());
        response.setShippingState(customer.getShippingState());
        response.setShippingCountry(customer.getShippingCountry());
        response.setShippingPostalCode(customer.getShippingPostalCode());

        response.setGstNumber(customer.getGstNumber());
        response.setPanNumber(customer.getPanNumber());
        response.setTaxPreference(customer.getTaxPreference());
        response.setCurrency(customer.getCurrency());
        response.setPaymentTerms(customer.getPaymentTerms());

        // Format creditLimit to two decimal places
        if (customer.getCreditLimit() != null) {
            response.setCreditLimit(String.format("%.2f", customer.getCreditLimit()));
        } else {
            response.setCreditLimit("0.00");
        }

        response.setStatus(customer.getStatus());
        response.setNotes(customer.getNotes());
        response.setCreatedBy(customer.getCreatedBy());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        response.setIsDeleted(customer.getIsDeleted());
        response.setDeletedAt(customer.getDeletedAt());

        return response;
    }
}
