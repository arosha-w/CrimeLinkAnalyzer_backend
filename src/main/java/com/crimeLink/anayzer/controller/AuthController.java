package com.crimeLink.anayzer.controller;

import com.crimeLink.anayzer.dto.*;
import com.crimeLink.anayzer.entity.RefreshToken;
import com.crimeLink.anayzer.entity.User;
import com.crimeLink.anayzer.service.AuthService;
import com.crimeLink.anayzer.service.JwtService;
import com.crimeLink.anayzer.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);
                    return ResponseEntity.ok(new TokenRefreshResponse(
                            true,
                            "Token refreshed successfully",
                            accessToken,
                            refreshTokenStr
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(401)
                        .body(new TokenRefreshResponse(
                                false,
                                "Invalid refresh token",
                                null,
                                null
                        )));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal User user) {
        if (user != null) {
            refreshTokenService.revokeAllUserTokens(user);
        }
        return ResponseEntity.ok().body(new Object() {
            public String message = "Logged out successfully";
        });
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(new Object() {
                public String message = "Not authenticated";
            });
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
