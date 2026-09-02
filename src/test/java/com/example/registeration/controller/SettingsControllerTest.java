package com.example.registeration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.registeration.dto.SettingsDTO;
import com.example.registeration.dto.SettingsSaveResponse;
import com.example.registeration.exception.SettingsValidationException;
import com.example.registeration.security.JwtService;
import com.example.registeration.service.SettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;

class SettingsControllerTest {

    private MockMvc mockMvc;
    private SettingsService settingsService;
    private JwtService jwtService;
    private ObjectMapper objectMapper;

    private UUID userId;
    private String token;

    @BeforeEach
    void setUp() {
        settingsService = org.mockito.Mockito.mock(SettingsService.class);
        jwtService = org.mockito.Mockito.mock(JwtService.class);
        objectMapper = new ObjectMapper();

        SettingsController controller = new SettingsController(settingsService, jwtService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        userId = UUID.randomUUID();
        token = "valid-test-token";
    }

    @Test
    void getSettings_WithoutAuthHeader_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/settings/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication credentials were not provided."));
    }

    @Test
    void getSettings_WithValidToken_ShouldReturnSettings() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        SettingsDTO dto = new SettingsDTO();
        SettingsDTO.OrganizationDTO org = new SettingsDTO.OrganizationDTO();
        org.setCompanyName("Prabhim Technologies (OPC) Pvt. Ltd.");
        org.setEmail("info@prabhimtechnologies.in");
        dto.setOrganization(org);

        when(settingsService.getSettings(userId)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/settings/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organization.companyName").value("Prabhim Technologies (OPC) Pvt. Ltd."))
                .andExpect(jsonPath("$.organization.email").value("info@prabhimtechnologies.in"));
    }

    @Test
    void updateSettings_WithValidPayload_ShouldReturnSuccessResponse() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        SettingsSaveResponse saveResponse = new SettingsSaveResponse("Settings saved successfully.", "2026-08-20T12:00:00Z");
        when(settingsService.updateSettings(eq(userId), any(SettingsDTO.class))).thenReturn(saveResponse);

        SettingsDTO request = new SettingsDTO();
        SettingsDTO.OrganizationDTO org = new SettingsDTO.OrganizationDTO();
        org.setCompanyName("Prabhim Technologies (OPC) Pvt. Ltd.");
        request.setOrganization(org);

        mockMvc.perform(put("/api/v1/settings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Settings saved successfully."))
                .andExpect(jsonPath("$.updatedAt").value("2026-08-20T12:00:00Z"));
    }

    @Test
    void updateSettings_WhenValidationFails_ShouldReturnBadRequestWithDetails() throws Exception {
        when(jwtService.extractUserId(token)).thenReturn(userId);

        Map<String, String> details = Map.of(
                "email", "Must be a valid email address.",
                "phone", "Must be a valid 10-digit phone number."
        );
        when(settingsService.updateSettings(eq(userId), any(SettingsDTO.class)))
                .thenThrow(new SettingsValidationException(details));

        SettingsDTO request = new SettingsDTO();

        mockMvc.perform(put("/api/v1/settings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.email").value("Must be a valid email address."))
                .andExpect(jsonPath("$.details.phone").value("Must be a valid 10-digit phone number."));
    }
}
