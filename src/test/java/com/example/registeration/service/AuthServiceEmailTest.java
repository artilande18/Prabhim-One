package com.example.registeration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.registeration.dto.RegisterRequest;
import com.example.registeration.dto.ResetPasswordRequest;
import com.example.registeration.entity.User;
import com.example.registeration.exception.InvalidOtpException;
import com.example.registeration.exception.OtpExpiredException;
import com.example.registeration.repository.SessionRepository;
import com.example.registeration.repository.UserRepository;
import com.example.registeration.security.JwtService;

class AuthServiceEmailTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private SessionRepository sessionRepository;
    private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        sessionRepository = mock(SessionRepository.class);
        emailService = mock(EmailService.class);

        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                sessionRepository,
                emailService
        );
    }

    @Test
    void register_ShouldSendVerificationEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setPhone("+123456");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User registeredUser = authService.register(request);

        // Assert
        assertNotNull(registeredUser);
        assertEquals("test@example.com", registeredUser.getEmail());
        assertNotNull(registeredUser.getVerificationOtp());
        assertTrue(registeredUser.getOtpExpiry().isAfter(LocalDateTime.now()));

        // Verify email was sent with correct arguments
        verify(emailService, times(1)).sendOtpEmail(
                eq("test@example.com"),
                eq(registeredUser.getVerificationOtp()),
                eq("registration")
        );
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void resendOtp_ShouldSendEmailBasedOnPurpose() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setEmailVerified(false);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.resendOtp("test@example.com", "password_reset");

        // Assert
        assertNotNull(user.getVerificationOtp());
        verify(emailService, times(1)).sendOtpEmail(
                eq("test@example.com"),
                eq(user.getVerificationOtp()),
                eq("password_reset")
        );
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void resetPassword_WithValidOtp_ShouldUpdatePassword() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setVerificationOtp("123456");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("NewSecuredPassword1!");
        request.setConfirmPassword("NewSecuredPassword1!");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewSecuredPassword1!")).thenReturn("newEncodedPassword");

        // Act
        authService.resetPassword(request);

        // Assert
        assertEquals("newEncodedPassword", user.getPassword());
        assertNull(user.getVerificationOtp());
        assertNull(user.getOtpExpiry());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void resetPassword_WithInvalidOtp_ShouldThrowException() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setVerificationOtp("123456");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("654321"); // wrong OTP
        request.setNewPassword("NewSecuredPassword1!");
        request.setConfirmPassword("NewSecuredPassword1!");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(InvalidOtpException.class, () -> authService.resetPassword(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_WithExpiredOtp_ShouldThrowException() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setVerificationOtp("123456");
        user.setOtpExpiry(LocalDateTime.now().minusMinutes(1)); // expired

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("NewSecuredPassword1!");
        request.setConfirmPassword("NewSecuredPassword1!");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(OtpExpiredException.class, () -> authService.resetPassword(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_WithMismatchedPasswords_ShouldThrowException() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setVerificationOtp("123456");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("NewSecuredPassword1!");
        request.setConfirmPassword("MismatchedPassword1!"); // mismatch

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
