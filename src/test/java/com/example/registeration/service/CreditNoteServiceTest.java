package com.example.registeration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.registeration.dto.CreateCreditNoteRequest;
import com.example.registeration.dto.CreditNoteItemRequest;
import com.example.registeration.dto.CreditNoteResponse;
import com.example.registeration.dto.UpdateCreditNoteRequest;
import com.example.registeration.entity.CreditNote;
import com.example.registeration.entity.CreditNoteItem;
import com.example.registeration.entity.User;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.CreditNoteItemRepository;
import com.example.registeration.repository.CreditNoteRepository;
import com.example.registeration.repository.CustomerRepository;
import com.example.registeration.repository.InvoiceRepository;
import com.example.registeration.repository.ProductRepository;
import com.example.registeration.repository.UserRepository;

class CreditNoteServiceTest {

    private CreditNoteRepository creditNoteRepository;
    private CreditNoteItemRepository creditNoteItemRepository;
    private UserRepository userRepository;
    private CustomerRepository customerRepository;
    private InvoiceRepository invoiceRepository;
    private ProductRepository productRepository;

    private CreditNoteService creditNoteService;

    @BeforeEach
    void setUp() {
        creditNoteRepository = mock(CreditNoteRepository.class);
        creditNoteItemRepository = mock(CreditNoteItemRepository.class);
        userRepository = mock(UserRepository.class);
        customerRepository = mock(CustomerRepository.class);
        invoiceRepository = mock(InvoiceRepository.class);
        productRepository = mock(ProductRepository.class);

        creditNoteService = new CreditNoteService(
                creditNoteRepository,
                creditNoteItemRepository,
                userRepository,
                customerRepository,
                invoiceRepository,
                productRepository
        );
    }

    @Test
    void createCreditNote_ShouldCalculateTotalsAndSave() {
        UUID customerId = UUID.randomUUID();
        User customer = new User();
        customer.setId(customerId);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setEmail("jane@example.com");

        UUID productId = UUID.randomUUID();
        CreditNoteItemRequest itemReq = new CreditNoteItemRequest();
        itemReq.setProductId(productId);
        itemReq.setQuantity(BigDecimal.valueOf(2));
        itemReq.setUnitPrice(BigDecimal.valueOf(100.00));
        itemReq.setTax(BigDecimal.valueOf(10.00));
        itemReq.setDiscount(BigDecimal.valueOf(20.00));

        CreateCreditNoteRequest request = new CreateCreditNoteRequest();
        request.setCustomer(customerId);
        request.setCreditNoteDate(LocalDate.now());
        request.setCurrency("INR");
        request.setItems(Collections.singletonList(itemReq));

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(userRepository.existsById(customerId)).thenReturn(true);
        when(creditNoteRepository.count()).thenReturn(5L);
        when(creditNoteRepository.save(any(CreditNote.class))).thenAnswer(i -> {
            CreditNote cn = i.getArgument(0);
            cn.setId(UUID.randomUUID());
            return cn;
        });

        CreditNoteResponse response = creditNoteService.createCreditNote(request);

        assertNotNull(response);
        assertEquals("CN-000006", response.getCreditNoteNumber());
        assertEquals(0, BigDecimal.valueOf(200.00).compareTo(response.getSubtotal()));
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(response.getDiscountTotal()));
        assertEquals(0, BigDecimal.valueOf(18.00).compareTo(response.getTaxTotal()));
        assertEquals(0, BigDecimal.valueOf(198.00).compareTo(response.getGrandTotal()));

        verify(creditNoteRepository, times(1)).save(any(CreditNote.class));
        verify(creditNoteItemRepository, times(1)).save(any(CreditNoteItem.class));
    }

    @Test
    void getCreditNotes_ShouldReturnPage() {
        CreditNote cn = new CreditNote();
        cn.setId(UUID.randomUUID());
        cn.setCreditNoteNumber("CN-000001");
        cn.setCustomerId(UUID.randomUUID());

        Page<CreditNote> page = new PageImpl<>(List.of(cn));
        when(creditNoteRepository.findWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<CreditNoteResponse> result = creditNoteService.getCreditNotes(null, null, null, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CN-000001", result.getContent().get(0).getCreditNoteNumber());
    }

    @Test
    void updateCreditNote_ShouldUpdateFields() {
        UUID id = UUID.randomUUID();
        CreditNote cn = new CreditNote();
        cn.setId(id);
        cn.setStatus("draft");
        cn.setCurrency("USD");

        when(creditNoteRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(cn));
        when(creditNoteRepository.save(any(CreditNote.class))).thenAnswer(i -> i.getArgument(0));

        UpdateCreditNoteRequest req = new UpdateCreditNoteRequest();
        req.setStatus("closed");
        req.setReason("Goods damaged");

        CreditNoteResponse res = creditNoteService.updateCreditNote(id, req);

        assertNotNull(res);
        assertEquals("closed", res.getStatus());
        assertEquals("Goods damaged", res.getReason());
    }

    @Test
    void deleteCreditNote_ShouldSoftDelete() {
        UUID id = UUID.randomUUID();
        CreditNote cn = new CreditNote();
        cn.setId(id);
        cn.setDeleted(false);

        when(creditNoteRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(cn));

        creditNoteService.deleteCreditNote(id);

        assertTrue(cn.isDeleted());
        assertNotNull(cn.getDeletedAt());
        verify(creditNoteRepository, times(1)).save(cn);
    }
}
