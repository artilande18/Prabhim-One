package com.example.registeration.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.registeration.dto.LoginRequest;
import com.example.registeration.dto.LoginResponse;
import com.example.registeration.dto.RegisterRequest;
import com.example.registeration.dto.SessionResponse;
import com.example.registeration.dto.UserResponse;
import com.example.registeration.dto.VerifyEmailRequest;
import com.example.registeration.entity.Session;
import com.example.registeration.entity.User;
import com.example.registeration.exception.InvalidOtpException;
import com.example.registeration.exception.OtpExpiredException;
import com.example.registeration.exception.UserAlreadyExistsException;
import com.example.registeration.exception.UserNotFoundException;
import com.example.registeration.repository.SessionRepository;
import com.example.registeration.repository.UserRepository;
import com.example.registeration.security.JwtService;

import com.example.registeration.dto.ResetPasswordRequest;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionRepository sessionRepository;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SessionRepository sessionRepository,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
        this.emailService = emailService;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);
        user.setActive(true);
        user.setStaff(false);
        user.setSuperuser(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setVerificationOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

        System.out.println("OTP for " + user.getEmail() + " = " + otp);
        emailService.sendOtpEmail(user.getEmail(), otp, "registration");

        return userRepository.save(user);
    }

    public User verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new UserAlreadyExistsException("Email is already verified");
        }
        if (user.getVerificationOtp() == null) {
            throw new InvalidOtpException("OTP not found");
        }
        if (!user.getVerificationOtp().equals(request.getOtp())) {
            throw new InvalidOtpException("Invalid OTP");
        }
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationOtp(null);
        user.setOtpExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // RESEND OTP
    public void resendOtp(String email, String purpose) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!purpose.equals("registration") && !purpose.equals("password_reset")) {
            throw new InvalidOtpException("Invalid purpose. Use registration or password_reset");
        }
        if (purpose.equals("registration") && user.isEmailVerified()) {
            throw new UserAlreadyExistsException("Email is already verified");
        }

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setVerificationOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        user.setUpdatedAt(LocalDateTime.now());

        System.out.println("New OTP for " + user.getEmail() + " = " + otp);
        emailService.sendOtpEmail(user.getEmail(), otp, purpose);

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidOtpException("Invalid credentials"); // reuse or create InvalidCredentialsException
        }

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not verified");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save session
        Session session = new Session();
        session.setUserId(user.getId());
        session.setIpAddress("127.0.0.1"); // TODO: capture real IP
        session.setDeviceType("Desktop");
        session.setBrowser("Chrome");
        session.setOs("Windows");
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setCurrent(true);
        sessionRepository.save(session);

        LoginResponse response = new LoginResponse();
        response.setAccess(accessToken);
        response.setRefresh(refreshToken);
        response.setUser(mapToUserResponse(user));

        // Optionally save session info in DB
        return response;
    }

    public Map<String, String> refreshToken(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String newAccess = jwtService.generateAccessTokenFromRefresh(refreshToken);
        String newRefresh = jwtService.rotateRefreshToken(refreshToken); // optional rotation

        return Map.of("access", newAccess, "refresh", newRefresh);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        sessionRepository.deleteByRefreshToken(refreshToken);
    }

    public List<SessionResponse> getSessions(UUID userId) {
        return sessionRepository.findByUserId(userId)
                .stream()
                .map(this::mapToSessionResponse)
                .toList();
    }

    @Transactional
    public void logoutAll(UUID userId) {
        sessionRepository.deleteByUserId(userId);
    }

    // Mapping helpers
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setProfileImage(user.getProfileImage());
        response.setEmailVerified(user.isEmailVerified());
        response.setActive(user.isActive());
        response.setStaff(user.isStaff());
        response.setSuperuser(user.isSuperuser());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private SessionResponse mapToSessionResponse(Session session) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setIpAddress(session.getIpAddress());
        response.setDeviceType(session.getDeviceType());
        response.setBrowser(session.getBrowser());
        response.setOs(session.getOs());
        response.setCreatedAt(session.getCreatedAt());
        response.setExpiresAt(session.getExpiresAt());
        response.setCurrent(session.isCurrent());
        return response;
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getVerificationOtp() == null) {
            throw new InvalidOtpException("OTP not found");
        }
        if (!user.getVerificationOtp().equals(request.getOtp())) {
            throw new InvalidOtpException("Invalid OTP");
        }
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP has expired");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationOtp(null);
        user.setOtpExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

}
