package com.crimeLink.anayzer.service;

import com.crimeLink.anayzer.dto.LoginRequest;
import com.crimeLink.anayzer.dto.LoginResponse;
import com.crimeLink.anayzer.dto.UserDTO;
import com.crimeLink.anayzer.entity.RefreshToken;
import com.crimeLink.anayzer.entity.User;
import com.crimeLink.anayzer.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuditService auditService;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            auditService.logLoginAttempt(null, request.getEmail(), false, 
                    "User not found", httpRequest);
            return new LoginResponse(false, "Invalid email or password", null, null, null);
        }

        User user = userOptional.get();

        // Check account status
        if (!"Active".equalsIgnoreCase(user.getStatus())) {
            auditService.logLoginAttempt(user.getUserId(), request.getEmail(), false, 
                    "Account not active", httpRequest);
            return new LoginResponse(false, "Account is not active", null, null, null);
        }

        // Verify password - handles both plain text (migration) and BCrypt
        boolean passwordMatches = verifyPassword(request.getPassword(), user.getPasswordHash(), user);

        if (!passwordMatches) {
            auditService.logLoginAttempt(user.getUserId(), request.getEmail(), false, 
                    "Invalid password", httpRequest);
            return new LoginResponse(false, "Invalid email or password", null, null, null);
        }

        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId());

        // Log successful login
        auditService.logLoginAttempt(user.getUserId(), request.getEmail(), true, 
                null, httpRequest);

        UserDTO userDTO = convertToDTO(user);

        return new LoginResponse(true, "Login successful", accessToken, 
                refreshToken.getToken(), userDTO);
    }

    private boolean verifyPassword(String plainPassword, String storedPassword, User user) {
        // Check if password is BCrypt hashed (starts with $2a$, $2b$, or $2y$)
        if (storedPassword.matches("^\\$2[aby]\\$.+$")) {
            // Already hashed, verify with BCrypt
            return passwordEncoder.matches(plainPassword, storedPassword);
        } else {
            // Plain text password (migration case)
            if (plainPassword.equals(storedPassword)) {
                // Password matches, hash it and update
                user.setPasswordHash(passwordEncoder.encode(plainPassword));
                userRepository.save(user);
                return true;
            }
            return false;
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setDob(user.getDob());
        dto.setGender(user.getGender());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole());
        dto.setBadgeNo(user.getBadgeNo());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        return dto;
    }
}
