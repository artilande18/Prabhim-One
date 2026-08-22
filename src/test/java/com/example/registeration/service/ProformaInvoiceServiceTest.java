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

import com.example.registeration.dto.CreateProformaInvoiceRequest;
import com.example.registeration.dto.ProformaInvoiceItemRequest;
import com.example.registeration.dto.ProformaInvoiceResponse;
import com.example.registeration.entity.ProformaInvoice;
import com.example.registeration.entity.ProformaInvoiceItem;
import com.example.registeration.entity.User;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.ProformaInvoiceItemRepository;
import com.example.registeration.repository.ProformaInvoiceRepository;
import com.example.registeration.repository.UserRepository;

class ProformaInvoiceServiceTest {

    private ProformaInvoiceRepository proformaInvoiceRepository;
    private ProformaInvoiceItemRepository proformaInvoiceItemRepository;
    private UserRepository userRepository;

    private ProformaInvoiceService proformaInvoiceService;

    @BeforeEach
    void setUp() {
        proformaInvoiceRepository = mock(ProformaInvoiceRepository.class);
        proformaInvoiceItemRepository = mock(ProformaInvoiceItemRepository.class);
        userRepository = mock(UserRepository.class);

        proformaInvoiceService = new ProformaInvoiceService(
                proformaInvoiceRepository,
                proformaInvoiceItemRepository,
                userRepository
        );
    }

    @Test
    void createProformaInvoice_ShouldCalculateTotalsAndSave() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        User customer = new User();
        customer.setId(customerId);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setEmail("jane@example.com");

        UUID productId = UUID.randomUUID();
        ProformaInvoiceItemRequest itemReq = new ProformaInvoiceItemRequest();
        itemReq.setProduct(productId);
        itemReq.setQuantity(BigDecimal.valueOf(2));
        itemReq.setUnitPrice(BigDecimal.valueOf(150.00));
        itemReq.setDiscount(BigDecimal.valueOf(10.00));
        itemReq.setTax(BigDecimal.valueOf(10.00)); // 10% tax

        CreateProformaInvoiceRequest request = new CreateProformaInvoiceRequest();
        request.setCustomer(customerId);
        request.setProformaInvoiceDate(LocalDate.now());
        request.setDueDate(LocalDate.now().plusDays(30));
        request.setCurrency("USD");
        request.setItems(Collections.singletonList(itemReq));

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(proformaInvoiceRepository.count()).thenReturn(3L);
        when(proformaInvoiceRepository.save(any(ProformaInvoice.class))).thenAnswer(inv -> {
            ProformaInvoice p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        // Act
        ProformaInvoiceResponse response = proformaInvoiceService.createProformaInvoice(request);

        // Assert
        assertNotNull(response);
        assertEquals("PFI-000004", response.getProformaInvoiceNumber());
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

        verify(proformaInvoiceRepository, times(1)).save(any(ProformaInvoice.class));
        verify(proformaInvoiceItemRepository, times(1)).save(any(ProformaInvoiceItem.class));
    }

    @Test
    void getProformaInvoice_WhenExistsAndNotDeleted_ShouldReturnResponse() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        ProformaInvoice pfi = new ProformaInvoice();
        pfi.setId(id);
        pfi.setCustomerId(customerId);
        pfi.setProformaInvoiceNumber("PFI-000101");
        pfi.setDeleted(false);

        User customer = new User();
        customer.setId(customerId);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setEmail("jane@example.com");

        when(proformaInvoiceRepository.findById(id)).thenReturn(Optional.of(pfi));
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Act
        ProformaInvoiceResponse response = proformaInvoiceService.getProformaInvoice(id);

        // Assert
        assertNotNull(response);
        assertEquals("PFI-000101", response.getProformaInvoiceNumber());
        assertEquals("Jane Doe", response.getCustomerName());
    }

    @Test
    void getProformaInvoice_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(proformaInvoiceRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> proformaInvoiceService.getProformaInvoice(id));
    }
}
