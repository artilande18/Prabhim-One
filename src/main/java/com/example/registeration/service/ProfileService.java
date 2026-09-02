package com.example.registeration.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.registeration.dto.ChangePasswordRequest;
import com.example.registeration.dto.ProfileResponse;
import com.example.registeration.dto.ProfileUpdateRequest;
import com.example.registeration.entity.User;
import com.example.registeration.exception.PasswordChangeException;
import com.example.registeration.exception.ProfileValidationException;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.exception.UserNotFoundException;
import com.example.registeration.repository.UserRepository;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapToProfileResponse(user);
    }

    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        validateProfileUpdateRequest(request, user.getId());

        // Update name - split into firstName and lastName
        String fullName = request.getName();
        if (fullName != null) {
            fullName = fullName.trim();
            int spaceIndex = fullName.indexOf(' ');
            if (spaceIndex != -1) {
                user.setFirstName(fullName.substring(0, spaceIndex));
                user.setLastName(fullName.substring(spaceIndex + 1).trim());
            } else {
                user.setFirstName(fullName);
                user.setLastName("");
            }
        }

        // Update fields
        user.setEmail(request.getEmail().toLowerCase().trim());

        if (request.getPhone() != null) {
            String sanitizedPhone = request.getPhone().replaceAll("[^0-9]", "");
            user.setPhone(sanitizedPhone);
        } else {
            user.setPhone(null);
        }

        user.setDesignation(request.getDesignation());
        user.setAvatar(request.getAvatar());
        
        // If profilePic is explicitly provided in the JSON body, update it.
        // If it's empty, we keep the existing or set to null?
        // Note: Field reference says profilePic is base64 encoded image.
        if (request.getProfilePic() != null) {
            user.setProfilePic(request.getProfilePic());
        }

        user.setTimeZone(request.getTimeZone());
        user.setTheme(request.getTheme());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return mapToProfileResponse(savedUser);
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
            throw new PasswordChangeException("Current password is required.");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new PasswordChangeException("Current password is incorrect.");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new PasswordChangeException("Password must be at least 8 characters long.");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordChangeException("New password and confirm password do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public String uploadPicture(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }

        // 2MB limit (2 * 1024 * 1024 = 2097152 bytes)
        if (file.getSize() > 2097152) {
            throw new IllegalArgumentException("File too large. Maximum allowed size is 2MB.");
        }

        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "image/png";
            }
            String base64Content = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
            
            user.setProfilePic(base64Content);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // Construct local serving URL
            return "/api/v1/profile/picture/" + user.getId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile picture: " + e.getMessage(), e);
        }
    }

    public void deletePicture(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setProfilePic(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public byte[] getProfilePictureBytes(UUID userId, Map<String, String> outContentType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile picture not found."));

        String base64Pic = user.getProfilePic();
        if (base64Pic == null || !base64Pic.startsWith("data:")) {
            throw new ResourceNotFoundException("Profile picture not found.");
        }

        try {
            int commaIndex = base64Pic.indexOf(',');
            if (commaIndex == -1) {
                throw new ResourceNotFoundException("Profile picture not found.");
            }

            String meta = base64Pic.substring(0, commaIndex);
            String base64Data = base64Pic.substring(commaIndex + 1);

            String contentType = "image/png";
            if (meta.contains(":") && meta.contains(";")) {
                contentType = meta.substring(meta.indexOf(':') + 1, meta.indexOf(';'));
            }
            outContentType.put("contentType", contentType);

            return Base64.getDecoder().decode(base64Data);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Profile picture not found.");
        }
    }

    private void validateProfileUpdateRequest(ProfileUpdateRequest request, UUID currentUserId) {
        Map<String, String> details = new LinkedHashMap<>();

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            details.put("name", "Name is required.");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            details.put("email", "Email is required.");
        } else {
            String email = request.getEmail().toLowerCase().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                details.put("email", "Must be a valid email address.");
            } else {
                userRepository.findByEmail(email).ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(currentUserId)) {
                        details.put("email", "Email already in use.");
                    }
                });
            }
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String sanitizedPhone = request.getPhone().replaceAll("[^0-9]", "");
            if (sanitizedPhone.length() != 10) {
                details.put("phone", "Must be a valid 10-digit mobile number.");
            }
        }

        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            List<String> allowedAvatars = List.of("👨💻", "👩💻", "🧑💼", "🚀", "💼", "⭐", "🎨", "🤖");
            if (!allowedAvatars.contains(request.getAvatar())) {
                details.put("avatar", "Avatar must be one of: 👨💻 👩💻 🧑💼 🚀 💼 ⭐ 🎨 🤖");
            }
        }

        if (request.getTheme() != null && !request.getTheme().trim().isEmpty()) {
            if (!"light".equalsIgnoreCase(request.getTheme()) && !"dark".equalsIgnoreCase(request.getTheme())) {
                details.put("theme", "Theme must be light or dark.");
            }
        }

        if (!details.isEmpty()) {
            throw new ProfileValidationException(details);
        }
    }

    private ProfileResponse mapToProfileResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        response.setName((firstName + " " + lastName).trim());

        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());

        // Role mapping
        if (user.isSuperuser()) {
            response.setRole("Admin / Business Owner");
        } else if (user.isStaff()) {
            response.setRole("Staff");
        } else {
            response.setRole("User");
        }

        response.setDesignation(user.getDesignation());
        response.setAvatar(user.getAvatar());
        response.setProfilePic(user.getProfilePic());
        response.setTimeZone(user.getTimeZone());
        response.setTheme(user.getTheme() != null ? user.getTheme().toLowerCase() : null);
        response.setAccountStatus(user.isActive() ? "Active" : "Inactive");
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }
}
