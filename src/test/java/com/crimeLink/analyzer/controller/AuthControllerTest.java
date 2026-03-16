package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.LoginRequest;
import com.crimeLink.analyzer.dto.LoginResponse;
import com.crimeLink.analyzer.dto.TokenRefreshRequest;
import com.crimeLink.analyzer.entity.RefreshToken;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.service.AuthService;
import com.crimeLink.analyzer.service.JwtService;
import com.crimeLink.analyzer.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController controller;

    @Mock private AuthService authService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private JwtService jwtService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void login_shouldReturn200_whenSuccess() throws Exception {
        LoginResponse response = new LoginResponse(true, "Login successful", "access", "refresh", null);
        when(authService.login(any(LoginRequest.class), any(HttpServletRequest.class))).thenReturn(response);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void login_shouldReturn401_whenFailure() throws Exception {
        LoginResponse response = new LoginResponse(false, "Invalid email or password", null, null, null);
        when(authService.login(any(LoginRequest.class), any(HttpServletRequest.class))).thenReturn(response);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_shouldReturn400_whenMissing() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshToken_shouldReturn200_whenValid() throws Exception {
        User user = new User();
        user.setUserId(1);

        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old");
        oldToken.setUser(user);

        RefreshToken newToken = new RefreshToken();
        newToken.setToken("new");
        newToken.setUser(user);

        when(refreshTokenService.findValidToken("old")).thenReturn(Optional.of(oldToken));
        when(refreshTokenService.rotateRefreshToken(oldToken)).thenReturn(newToken);
        when(jwtService.generateToken(user)).thenReturn("access-new");

        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("old");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-new"));
    }

    @Test
    void logout_shouldReturn200() throws Exception {
        User principal = new User();
        principal.setEmail("user@test.com");
        principal.setPasswordHash("pw");
        principal.setRole("Admin");
        principal.setStatus("Active");

        mockMvc.perform(post("/api/auth/logout").with(user(principal)))
                .andExpect(status().isOk());
    }

    @Test
    void getCurrentUser_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}