package com.example.registeration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.registeration.dto.CreateEstimateRequest;
import com.example.registeration.dto.EstimateItemRequest;
import com.example.registeration.dto.EstimateResponse;
import com.example.registeration.dto.UpdateEstimateRequest;
import com.example.registeration.entity.Estimate;
import com.example.registeration.entity.EstimateItem;
import com.example.registeration.entity.User;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.EstimateItemRepository;
import com.example.registeration.repository.EstimateRepository;
import com.example.registeration.repository.UserRepository;

class EstimateServiceTest {

    private EstimateRepository estimateRepository;
    private EstimateItemRepository estimateItemRepository;
    private UserRepository userRepository;

    private EstimateService estimateService;

    @BeforeEach
    void setUp() {
        estimateRepository = mock(EstimateRepository.class);
        estimateItemRepository = mock(EstimateItemRepository.class);
        userRepository = mock(UserRepository.class);

        estimateService = new EstimateService(
                estimateRepository,
                estimateItemRepository,
                userRepository
        );
    }

    @Test
    void createEstimate_ShouldCalculateTotalsAndSave() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        User customer = new User();
        customer.setId(customerId);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setEmail("jane@example.com");

        UUID productId = UUID.randomUUID();
        EstimateItemRequest itemReq = new EstimateItemRequest();
        itemReq.setProduct(productId);
        itemReq.setQuantity(BigDecimal.valueOf(2));
        itemReq.setUnitPrice(BigDecimal.valueOf(150.00));
        itemReq.setDiscount(BigDecimal.valueOf(10.00));
        itemReq.setTax(BigDecimal.valueOf(10.00)); // 10% tax

        CreateEstimateRequest request = new CreateEstimateRequest();
        request.setCustomer(customerId);
        request.setEstimateDate(LocalDate.now());
        request.setExpiryDate(LocalDate.now().plusDays(30));
        request.setCurrency("USD");
        request.setItems(Collections.singletonList(itemReq));

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(estimateRepository.count()).thenReturn(5L);
        when(estimateRepository.save(any(Estimate.class))).thenAnswer(inv -> {
            Estimate e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        // Act
        EstimateResponse response = estimateService.createEstimate(request);

        // Assert
        assertNotNull(response);
        assertEquals("EST-000006", response.getEstimateNumber());
        assertEquals(customerId, response.getCustomer());
        assertEquals("Jane Doe", response.getCustomerName());
        assertEquals("jane@example.com", response.getCreatedByEmail());

        // Calculations:
        // Subtotal = 150 * 2 = 300
        // Taxable = 300 - 10 = 290
        // Tax = 290 * 0.10 = 29
        // GrandTotal = 290 + 29 = 319
        assertEquals(0, BigDecimal.valueOf(300.00).compareTo(response.getSubtotal()));
        assertEquals(0, BigDecimal.valueOf(10.00).compareTo(response.getDiscountTotal()));
        assertEquals(0, BigDecimal.valueOf(29.00).compareTo(response.getTaxTotal()));
        assertEquals(0, BigDecimal.valueOf(319.00).compareTo(response.getGrandTotal()));

        verify(estimateRepository, times(1)).save(any(Estimate.class));
        verify(estimateItemRepository, times(1)).save(any(EstimateItem.class));
    }

    @Test
    void getEstimate_WhenExistsAndNotDeleted_ShouldReturnResponse() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Estimate estimate = new Estimate();
        estimate.setId(id);
        estimate.setCustomerId(customerId);
        estimate.setEstimateNumber("EST-000101");
        estimate.setDeleted(false);

        User customer = new User();
        customer.setId(customerId);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setEmail("jane@example.com");

        when(estimateRepository.findById(id)).thenReturn(Optional.of(estimate));
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Act
        EstimateResponse response = estimateService.getEstimate(id);

        // Assert
        assertNotNull(response);
        assertEquals("EST-000101", response.getEstimateNumber());
        assertEquals("Jane Doe", response.getCustomerName());
    }

    @Test
    void getEstimate_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(estimateRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> estimateService.getEstimate(id));
    }

    @Test
    void updateEstimate_WhenDraft_ShouldUpdateAllowedFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        Estimate estimate = new Estimate();
        estimate.setId(id);
        estimate.setStatus("draft");
        estimate.setDeleted(false);

        UpdateEstimateRequest request = new UpdateEstimateRequest();
        request.setExpiryDate(LocalDate.now().plusDays(45));
        request.setNotes("Updated Notes");
        request.setTermsAndConditions("Updated Terms");
        request.setStatus("sent");

        when(estimateRepository.findById(id)).thenReturn(Optional.of(estimate));
        when(estimateRepository.save(any(Estimate.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        EstimateResponse response = estimateService.updateEstimate(id, request);

        // Assert
        assertNotNull(response);
        assertEquals("sent", response.getStatus());
        verify(estimateRepository, times(1)).save(estimate);
        assertEquals(request.getExpiryDate(), estimate.getExpiryDate());
        assertEquals("Updated Notes", estimate.getNotes());
        assertEquals("Updated Terms", estimate.getTermsAndConditions());
    }

    @Test
    void updateEstimate_WhenNotDraft_ShouldThrowIllegalArgumentException() {
        // Arrange
        UUID id = UUID.randomUUID();
        Estimate estimate = new Estimate();
        estimate.setId(id);
        estimate.setStatus("accepted");
        estimate.setDeleted(false);

        UpdateEstimateRequest request = new UpdateEstimateRequest();

        when(estimateRepository.findById(id)).thenReturn(Optional.of(estimate));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> estimateService.updateEstimate(id, request));
    }

    @Test
    void deleteEstimate_WhenDraft_ShouldMarkAsDeleted() {
        // Arrange
        UUID id = UUID.randomUUID();
        Estimate estimate = new Estimate();
        estimate.setId(id);
        estimate.setStatus("draft");
        estimate.setDeleted(false);

        when(estimateRepository.findById(id)).thenReturn(Optional.of(estimate));

        // Act
        estimateService.deleteEstimate(id);

        // Assert
        assertTrue(estimate.isDeleted());
        assertNotNull(estimate.getDeletedAt());
        verify(estimateRepository, times(1)).save(estimate);
    }
}
