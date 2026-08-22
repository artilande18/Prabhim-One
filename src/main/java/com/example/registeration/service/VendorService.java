package com.example.registeration.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.registeration.dto.VendorRequest;
import com.example.registeration.dto.VendorResponse;
import com.example.registeration.entity.Vendor;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.VendorRepository;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Transactional
    public VendorResponse createVendor(VendorRequest request, UUID userId) {
        Vendor vendor = new Vendor();
        updateVendorFields(vendor, request);
        vendor.setCreatedBy(userId);
        vendor.setCreatedAt(LocalDateTime.now());
        vendor.setUpdatedAt(LocalDateTime.now());
        vendor.setOrganization(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")); // default org UUID

        Vendor saved = vendorRepository.save(vendor);
        return mapToVendorResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> listVendors(UUID userId) {
        return vendorRepository.findAll().stream()
                .filter(v -> !v.getIsDeleted())
                .map(this::mapToVendorResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendor(UUID id, UUID userId) {
        Vendor vendor = vendorRepository.findById(id)
                .filter(v -> !v.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return mapToVendorResponse(vendor);
    }

    @Transactional
    public VendorResponse updateVendor(UUID id, VendorRequest request, UUID userId) {
        Vendor vendor = vendorRepository.findById(id)
                .filter(v -> !v.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        updateVendorFields(vendor, request);
        vendor.setUpdatedAt(LocalDateTime.now());

        Vendor saved = vendorRepository.save(vendor);
        return mapToVendorResponse(saved);
    }

    @Transactional
    public void deleteVendor(UUID id, UUID userId) {
        Vendor vendor = vendorRepository.findById(id)
                .filter(v -> !v.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        vendor.setIsDeleted(true);
        vendor.setDeletedAt(LocalDateTime.now());
        vendor.setUpdatedAt(LocalDateTime.now());
        vendorRepository.save(vendor);
    }

    private void updateVendorFields(Vendor vendor, VendorRequest request) {
        if (request.getDisplayName() != null) vendor.setDisplayName(request.getDisplayName());
        if (request.getCompanyName() != null) vendor.setCompanyName(request.getCompanyName());
        if (request.getFirstName() != null) vendor.setFirstName(request.getFirstName());
        if (request.getLastName() != null) vendor.setLastName(request.getLastName());
        if (request.getEmail() != null) vendor.setEmail(request.getEmail());
        if (request.getPrimaryPhone() != null) vendor.setPrimaryPhone(request.getPrimaryPhone());
        if (request.getSecondaryPhone() != null) vendor.setSecondaryPhone(request.getSecondaryPhone());
        if (request.getWebsite() != null) vendor.setWebsite(request.getWebsite());

        if (request.getBillingAttention() != null) vendor.setBillingAttention(request.getBillingAttention());
        if (request.getBillingAddressLine1() != null) vendor.setBillingAddressLine1(request.getBillingAddressLine1());
        if (request.getBillingAddressLine2() != null) vendor.setBillingAddressLine2(request.getBillingAddressLine2());
        if (request.getBillingCity() != null) vendor.setBillingCity(request.getBillingCity());
        if (request.getBillingState() != null) vendor.setBillingState(request.getBillingState());
        if (request.getBillingCountry() != null) vendor.setBillingCountry(request.getBillingCountry());
        if (request.getBillingPostalCode() != null) vendor.setBillingPostalCode(request.getBillingPostalCode());

        if (request.getShippingSameAsBilling() != null) vendor.setShippingSameAsBilling(request.getShippingSameAsBilling());
        if (request.getShippingAttention() != null) vendor.setShippingAttention(request.getShippingAttention());
        if (request.getShippingAddressLine1() != null) vendor.setShippingAddressLine1(request.getShippingAddressLine1());
        if (request.getShippingAddressLine2() != null) vendor.setShippingAddressLine2(request.getShippingAddressLine2());
        if (request.getShippingCity() != null) vendor.setShippingCity(request.getShippingCity());
        if (request.getShippingState() != null) vendor.setShippingState(request.getShippingState());
        if (request.getShippingCountry() != null) vendor.setShippingCountry(request.getShippingCountry());
        if (request.getShippingPostalCode() != null) vendor.setShippingPostalCode(request.getShippingPostalCode());

        if (request.getGstNumber() != null) vendor.setGstNumber(request.getGstNumber());
        if (request.getPanNumber() != null) vendor.setPanNumber(request.getPanNumber());
        if (request.getTaxPreference() != null) vendor.setTaxPreference(request.getTaxPreference());
        if (request.getCurrency() != null) vendor.setCurrency(request.getCurrency());
        if (request.getPaymentTerms() != null) vendor.setPaymentTerms(request.getPaymentTerms());
        if (request.getNotes() != null) vendor.setNotes(request.getNotes());
    }

    private VendorResponse mapToVendorResponse(Vendor vendor) {
        VendorResponse response = new VendorResponse();
        response.setId(vendor.getId());
        response.setOrganization(vendor.getOrganization());
        response.setDisplayName(vendor.getDisplayName());
        response.setCompanyName(vendor.getCompanyName());
        response.setFirstName(vendor.getFirstName());
        response.setLastName(vendor.getLastName());
        response.setEmail(vendor.getEmail());
        response.setPrimaryPhone(vendor.getPrimaryPhone());
        response.setSecondaryPhone(vendor.getSecondaryPhone());
        response.setWebsite(vendor.getWebsite());

        response.setBillingAttention(vendor.getBillingAttention());
        response.setBillingAddressLine1(vendor.getBillingAddressLine1());
        response.setBillingAddressLine2(vendor.getBillingAddressLine2());
        response.setBillingCity(vendor.getBillingCity());
        response.setBillingState(vendor.getBillingState());
        response.setBillingCountry(vendor.getBillingCountry());
        response.setBillingPostalCode(vendor.getBillingPostalCode());

        response.setShippingSameAsBilling(vendor.getShippingSameAsBilling());
        response.setShippingAttention(vendor.getShippingAttention());
        response.setShippingAddressLine1(vendor.getShippingAddressLine1());
        response.setShippingAddressLine2(vendor.getShippingAddressLine2());
        response.setShippingCity(vendor.getShippingCity());
        response.setShippingState(vendor.getShippingState());
        response.setShippingCountry(vendor.getShippingCountry());
        response.setShippingPostalCode(vendor.getShippingPostalCode());

        response.setGstNumber(vendor.getGstNumber());
        response.setPanNumber(vendor.getPanNumber());
        response.setTaxPreference(vendor.getTaxPreference());
        response.setCurrency(vendor.getCurrency());
        response.setPaymentTerms(vendor.getPaymentTerms());

        response.setStatus(vendor.getStatus());
        response.setNotes(vendor.getNotes());

        response.setCreatedBy(vendor.getCreatedBy());
        response.setCreatedAt(vendor.getCreatedAt());
        response.setUpdatedAt(vendor.getUpdatedAt());
        response.setIsDeleted(vendor.getIsDeleted());
        response.setDeletedAt(vendor.getDeletedAt());

        return response;
    }
}
