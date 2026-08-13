package com.example.registeration.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.registeration.dto.RegisterRequest;
import com.example.registeration.dto.VerifyEmailRequest;
import com.example.registeration.entity.User;
import com.example.registeration.exception.EmailAlreadyExistsException;
import com.example.registeration.exception.EmailAlreadyVerifiedException;
import com.example.registeration.exception.InvalidOtpException;
import com.example.registeration.exception.InvalidPurposeException;
import com.example.registeration.exception.OtpExpiredException;
import com.example.registeration.exception.UserNotFoundException;
import com.example.registeration.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());


        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setEmailVerified(false);
        user.setActive(true);
        user.setStaff(false);
        user.setSuperuser(false);

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        String otp = String.valueOf(
                (int) (Math.random() * 900000) + 100000
        );

        user.setVerificationOtp(otp);

        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

        user.setEmailVerified(false);

        // Temporary: print OTP in console
        System.out.println("OTP for " + user.getEmail() + " = " + otp);

        return userRepository.save(user);

    }

    // VERIFY EMAIL
    public User verifyEmail(VerifyEmailRequest request) {

        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()
                        -> new UserNotFoundException("User not found")
                );

        // 2. Check if already verified
        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        // 3. Check OTP
        if (user.getVerificationOtp() == null) {
            throw new InvalidOtpException("OTP not found");
        }

        if (!user.getVerificationOtp().equals(request.getOtp())) {
            throw new InvalidOtpException("Invalid OTP");
        }

        // 4. Check OTP expiry
        if (user.getOtpExpiry() == null
                || user.getOtpExpiry().isBefore(LocalDateTime.now())) {

            throw new OtpExpiredException("OTP has expired");
        }

        // 5. Verify email
        user.setEmailVerified(true);

        // 6. Remove OTP after successful verification
        user.setVerificationOtp(null);
        user.setOtpExpiry(null);

        user.setUpdatedAt(LocalDateTime.now());

    
        return userRepository.save(user);
    }


    // RESEND OTP

    public void resendOtp(String email, String purpose) {

        // 1. Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new UserNotFoundException("User not found")
                );

        // 2. Check purpose
        if (!purpose.equals("registration")
                && !purpose.equals("password_reset")) {

            throw new InvalidPurposeException(
                    "Invalid purpose. Use registration or password_reset"
            );
        }

        // 3. For registration, don't resend if already verified
        if (purpose.equals("registration")
                && user.isEmailVerified()) {

            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        // 4. Generate new 6-digit OTP
        String otp = String.valueOf(
                (int) (Math.random() * 900000) + 100000
        );

        // 5. Save new OTP
        user.setVerificationOtp(otp);

        // 6. OTP valid for 10 minutes
        user.setOtpExpiry(
                LocalDateTime.now().plusMinutes(10)
        );

        user.setUpdatedAt(LocalDateTime.now());

        // 7. Print OTP temporarily for testing
        System.out.println(
                "New OTP for " + user.getEmail() + " = " + otp
        );

        // 8. Save user
        userRepository.save(user);
    }

}
