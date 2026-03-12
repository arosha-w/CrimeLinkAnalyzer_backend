package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.*;
import com.crimeLink.analyzer.entity.RefreshToken;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.service.AuthService;
import com.crimeLink.analyzer.service.JwtService;
import com.crimeLink.analyzer.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
            LoginResponse response = authService.login(request, httpRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        String refreshTokenStr = request.getRefreshToken();

        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            return ResponseEntity.badRequest().body(new TokenRefreshResponse(
                    false,
                    "Refresh token is required",
                    null,
                    null
            ));
        }

        return refreshTokenService.findValidToken(refreshTokenStr)
                .map(validToken -> {
                    RefreshToken rotated = refreshTokenService.rotateRefreshToken(validToken);
                    User user = rotated.getUser();
                    String accessToken = jwtService.generateToken(user);

                    return ResponseEntity.ok(new TokenRefreshResponse(
                            true,
                            "Token refreshed successfully",
                            accessToken,
                            rotated.getToken()
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(new TokenRefreshResponse(
                        false,
                        "Invalid or expired refresh token",
                        null,
                        null
                )));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal User user) {
        if (user != null) {
            refreshTokenService.revokeAllUserTokens(user);
        }
        return ResponseEntity.ok().body(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        UserDTO userDTO = new UserDTO(
                user.getUserId(),
                user.getName(),
                user.getDob(),
                user.getGender(),
                user.getAddress(),
                user.getRole(),
                user.getBadgeNo(),
                user.getEmail(),
                user.getStatus()
        );

        return ResponseEntity.ok(userDTO);
    }
}
