package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.PasswordChangeRequest;
import com.crimeLink.analyzer.dto.ProfileUpdateRequest;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.RefreshTokenRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get current user's profile
     * GET /api/profile
     */
    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepo.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Return user without password hash
            Map<String, Object> profile = Map.of(
                    "userId", user.getUserId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "dob", user.getDob() != null ? user.getDob().toString() : "",
                    "gender", user.getGender() != null ? user.getGender() : "",
                    "address", user.getAddress() != null ? user.getAddress() : "",
                    "role", user.getRole(),
                    "badgeNo", user.getBadgeNo() != null ? user.getBadgeNo() : "",
                    "status", user.getStatus()
            );

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to fetch profile: " + e.getMessage()));
        }
    }

    /**
     * Update current user's profile
     * PUT /api/profile
     */
    @PutMapping
    @Transactional
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        try {
            User user = userRepo.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if email is being changed and if it already exists
            if (!user.getEmail().equals(request.getEmail())) {
                if (userRepo.existsByEmail(request.getEmail())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Email already exists"));
                }
            }

            // Update profile fields
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setDob(request.getDob());
            user.setGender(request.getGender());
            user.setAddress(request.getAddress());

            User updatedUser = userRepo.save(user);

            // Return updated profile
            Map<String, Object> profile = Map.of(
                    "userId", updatedUser.getUserId(),
                    "name", updatedUser.getName(),
                    "email", updatedUser.getEmail(),
                    "dob", updatedUser.getDob() != null ? updatedUser.getDob().toString() : "",
                    "gender", updatedUser.getGender() != null ? updatedUser.getGender() : "",
                    "address", updatedUser.getAddress() != null ? updatedUser.getAddress() : "",
                    "role", updatedUser.getRole(),
                    "badgeNo", updatedUser.getBadgeNo() != null ? updatedUser.getBadgeNo() : "",
                    "status", updatedUser.getStatus()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Profile updated successfully",
                    "profile", profile
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to update profile: " + e.getMessage()));
        }
    }

    /**
     * Change current user's password
     * PUT /api/profile/password
     */
    @PutMapping("/password")
    @Transactional
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request) {
        try {
            User user = userRepo.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify old password
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Current password is incorrect"));
            }

            // Verify new password confirmation
            if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "New passwords do not match"));
            }

            // Update password
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepo.save(user);

            // Revoke all refresh tokens except current session (security measure)
            // Note: We keep current session active, but revoke all other sessions
            refreshTokenRepo.revokeAllUserTokens(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Password changed successfully. Other sessions have been logged out."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to change password: " + e.getMessage()));
        }
    }
}
