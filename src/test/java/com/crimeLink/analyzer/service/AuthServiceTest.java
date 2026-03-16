package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.LoginRequest;
import com.crimeLink.analyzer.dto.LoginResponse;
import com.crimeLink.analyzer.entity.RefreshToken;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuditService auditService;
    @Mock
    private HttpServletRequest httpServletRequest;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setUserId(1);
        activeUser.setName("Test User");
        activeUser.setEmail("test@email.com");
        activeUser.setPasswordHash("$2a$10$hashedPassword");
        activeUser.setStatus("Active");
        activeUser.setRole("Admin");
        ReflectionTestUtils.setField(authService, "userRepository", userRepository);
        ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(authService, "jwtService", jwtService);
        ReflectionTestUtils.setField(authService, "refreshTokenService", refreshTokenService);
        ReflectionTestUtils.setField(authService, "auditService", auditService);
    }

    @Test
    void login_shouldReturnFailure_whenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@email.com");
        request.setPassword("123456");

        when(userRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

        LoginResponse response = authService.login(request, httpServletRequest);

        assertFalse(response.isSuccess());
        assertEquals("Invalid email or password", response.getMessage());
        verify(auditService).logLoginAttempt(null, "missing@email.com", false, "User not found", httpServletRequest);
    }

    @Test
    void login_shouldReturnFailure_whenUserInactive() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");
        request.setPassword("123456");

        activeUser.setStatus("Inactive");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(activeUser));

        LoginResponse response = authService.login(request, httpServletRequest);

        assertFalse(response.isSuccess());
        assertEquals("Account is not active", response.getMessage());
        verify(auditService).logLoginAttempt(activeUser.getUserId(), "test@email.com", false, "Account not active", httpServletRequest);
    }

    @Test
    void login_shouldReturnFailure_whenHashedPasswordInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", activeUser.getPasswordHash())).thenReturn(false);

        LoginResponse response = authService.login(request, httpServletRequest);

        assertFalse(response.isSuccess());
        assertEquals("Invalid email or password", response.getMessage());
        verify(auditService).logLoginAttempt(activeUser.getUserId(), "test@email.com", false, "Invalid password", httpServletRequest);
    }

    @Test
    void login_shouldReturnSuccess_whenHashedPasswordValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");
        request.setPassword("correct");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("correct", activeUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(activeUser)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(activeUser.getUserId())).thenReturn(refreshToken);

        LoginResponse response = authService.login(request, httpServletRequest);

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(activeUser.getUserId(), response.getUser().getUserId());
        verify(auditService).logLoginAttempt(activeUser.getUserId(), "test@email.com", true, null, httpServletRequest);
    }

    @Test
    void login_shouldMigratePlainTextPassword_whenMatched() {
        LoginRequest request = new LoginRequest();
        request.setEmail("plain@email.com");
        request.setPassword("plain123");

        activeUser.setEmail("plain@email.com");
        activeUser.setPasswordHash("plain123");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");

        when(userRepository.findByEmail("plain@email.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.encode("plain123")).thenReturn("$2a$10$newHash");
        when(jwtService.generateToken(activeUser)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(activeUser.getUserId())).thenReturn(refreshToken);

        LoginResponse response = authService.login(request, httpServletRequest);

        assertTrue(response.isSuccess());
        assertEquals("$2a$10$newHash", activeUser.getPasswordHash());
        verify(userRepository).save(activeUser);
    }

    @Test
    void login_shouldFail_whenPlainTextPasswordNotMatched() {
        LoginRequest request = new LoginRequest();
        request.setEmail("plain@email.com");
        request.setPassword("wrong");

        activeUser.setEmail("plain@email.com");
        activeUser.setPasswordHash("plain123");

        when(userRepository.findByEmail("plain@email.com")).thenReturn(Optional.of(activeUser));

        LoginResponse response = authService.login(request, httpServletRequest);

        assertFalse(response.isSuccess());
        assertEquals("Invalid email or password", response.getMessage());
        verify(userRepository, never()).save(any());
    }
}