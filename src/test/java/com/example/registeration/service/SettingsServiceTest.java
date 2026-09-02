package com.example.registeration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.registeration.dto.SettingsDTO;
import com.example.registeration.dto.SettingsSaveResponse;
import com.example.registeration.entity.OrganizationSetting;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.exception.SettingsValidationException;
import com.example.registeration.repository.OrganizationSettingRepository;
import com.example.registeration.repository.UserRepository;

class SettingsServiceTest {

    private OrganizationSettingRepository organizationSettingRepository;
    private UserRepository userRepository;
    private SettingsService settingsService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        organizationSettingRepository = mock(OrganizationSettingRepository.class);
        userRepository = mock(UserRepository.class);
        settingsService = new SettingsService(organizationSettingRepository, userRepository);
        userId = UUID.randomUUID();
    }

    @Test
    void getSettings_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> settingsService.getSettings(userId));
    }

    @Test
    void getSettings_WhenNoSettingsExist_ShouldCreateAndReturnDefaults() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(organizationSettingRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(organizationSettingRepository.save(any(OrganizationSetting.class))).thenAnswer(i -> i.getArgument(0));

        SettingsDTO dto = settingsService.getSettings(userId);

        assertNotNull(dto);
        assertNotNull(dto.getOrganization());
        assertEquals("Prabhim Technologies (OPC) Pvt. Ltd.", dto.getOrganization().getCompanyName());
        assertEquals("info@prabhimtechnologies.in", dto.getOrganization().getEmail());
        assertEquals("+91-9403301412", dto.getOrganization().getPhone());

        assertNotNull(dto.getInvoices());
        assertEquals("INV-", dto.getInvoices().getInvoicePrefix());
        assertEquals(true, dto.getInvoices().getInvoiceAutoNumber());
        assertEquals(18, dto.getInvoices().getTaxRate());

        assertNotNull(dto.getEstimates());
        assertEquals("EST-", dto.getEstimates().getEstimatePrefix());

        assertNotNull(dto.getCreditNotes());
        assertEquals("CN-", dto.getCreditNotes().getCreditNotePrefix());

        assertNotNull(dto.getProforma());
        assertEquals("PI-", dto.getProforma().getProformaPrefix());

        assertNotNull(dto.getTerms());
        assertNotNull(dto.getCustomers());
        assertNotNull(dto.getPayments());
        assertNotNull(dto.getExpenses());
        assertNotNull(dto.getTax());
        assertNotNull(dto.getPreferences());

        verify(organizationSettingRepository, times(1)).save(any(OrganizationSetting.class));
    }

    @Test
    void getSettings_WhenSettingsExist_ShouldReturnExistingSettings() {
        when(userRepository.existsById(userId)).thenReturn(true);

        OrganizationSetting existing = new OrganizationSetting();
        existing.setUserId(userId);
        existing.setCompanyName("Custom Enterprise");
        existing.setEmail("contact@custom.com");
        existing.setPhone("9876543210");
        existing.setInvoicePrefix("CUST-");
        existing.setTaxRate(12.0);

        when(organizationSettingRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        SettingsDTO dto = settingsService.getSettings(userId);

        assertNotNull(dto);
        assertEquals("Custom Enterprise", dto.getOrganization().getCompanyName());
        assertEquals("contact@custom.com", dto.getOrganization().getEmail());
        assertEquals("CUST-", dto.getInvoices().getInvoicePrefix());
        assertEquals(12, dto.getInvoices().getTaxRate());
    }

    @Test
    void updateSettings_WhenValid_ShouldUpdateAndReturnResponse() {
        when(userRepository.existsById(userId)).thenReturn(true);

        OrganizationSetting existing = new OrganizationSetting();
        existing.setUserId(userId);
        existing.setCompanyName("Old Name");
        when(organizationSettingRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(organizationSettingRepository.save(any(OrganizationSetting.class))).thenAnswer(i -> i.getArgument(0));

        SettingsDTO request = new SettingsDTO();
        SettingsDTO.OrganizationDTO org = new SettingsDTO.OrganizationDTO();
        org.setCompanyName("Updated Corp");
        org.setEmail("updated@prabhimtechnologies.in");
        org.setPhone("+91-9876543210");
        request.setOrganization(org);

        SettingsDTO.InvoiceDTO inv = new SettingsDTO.InvoiceDTO();
        inv.setInvoiceDefaultTerms("Net 30");
        request.setInvoices(inv);

        SettingsSaveResponse response = settingsService.updateSettings(userId, request);

        assertNotNull(response);
        assertEquals("Settings saved successfully.", response.getMessage());
        assertNotNull(response.getUpdatedAt());
        assertEquals("Updated Corp", existing.getCompanyName());
        assertEquals("Net 30", existing.getInvoiceDefaultTerms());
    }

    @Test
    void updateSettings_WhenInvalidEmail_ShouldThrowValidationException() {
        when(userRepository.existsById(userId)).thenReturn(true);

        SettingsDTO request = new SettingsDTO();
        SettingsDTO.OrganizationDTO org = new SettingsDTO.OrganizationDTO();
        org.setEmail("invalid-email-format");
        request.setOrganization(org);

        SettingsValidationException ex = assertThrows(
                SettingsValidationException.class,
                () -> settingsService.updateSettings(userId, request)
        );

        assertNotNull(ex.getDetails());
        assertTrue(ex.getDetails().containsKey("email"));
        assertEquals("Must be a valid email address.", ex.getDetails().get("email"));
    }

    @Test
    void updateSettings_WhenInvalidPhone_ShouldThrowValidationException() {
        when(userRepository.existsById(userId)).thenReturn(true);

        SettingsDTO request = new SettingsDTO();
        SettingsDTO.OrganizationDTO org = new SettingsDTO.OrganizationDTO();
        org.setPhone("123"); // invalid phone number
        request.setOrganization(org);

        SettingsValidationException ex = assertThrows(
                SettingsValidationException.class,
                () -> settingsService.updateSettings(userId, request)
        );

        assertNotNull(ex.getDetails());
        assertTrue(ex.getDetails().containsKey("phone"));
        assertEquals("Must be a valid 10-digit phone number.", ex.getDetails().get("phone"));
    }
}
