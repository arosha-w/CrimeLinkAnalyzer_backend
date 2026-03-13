package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.RefreshToken;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.RefreshTokenRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService service;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "refreshTokenDuration", 3600000L);
        ReflectionTestUtils.setField(service, "refreshTokenRepository", refreshTokenRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
    }

    @Test
    void createRefreshToken_shouldCreateByUserId() {
        User user = new User();
        user.setUserId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = service.createRefreshToken(1);

        assertNotNull(result.getToken());
        assertEquals(user, result.getUser());
        assertFalse(result.getRevoked());
    }

    @Test
    void findByToken_shouldDelegate() {
        RefreshToken token = new RefreshToken();
        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = service.findByToken("abc");

        assertTrue(result.isPresent());
    }

    @Test
    void findValidToken_shouldReturnEmpty_whenNotFound() {
        when(refreshTokenRepository.findByTokenWithUser("abc")).thenReturn(Optional.empty());

        Optional<RefreshToken> result = service.findValidToken("abc");

        assertTrue(result.isEmpty());
    }

    @Test
    void findValidToken_shouldDeleteAndReturnEmpty_whenExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        token.setRevoked(false);

        when(refreshTokenRepository.findByTokenWithUser("abc")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = service.findValidToken("abc");

        assertTrue(result.isEmpty());
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void findValidToken_shouldReturnEmpty_whenRevoked() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        token.setRevoked(true);

        when(refreshTokenRepository.findByTokenWithUser("abc")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = service.findValidToken("abc");

        assertTrue(result.isEmpty());
    }

    @Test
    void findValidToken_shouldReturnToken_whenValid() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        token.setRevoked(false);

        when(refreshTokenRepository.findByTokenWithUser("abc")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = service.findValidToken("abc");

        assertTrue(result.isPresent());
    }

    @Test
    void rotateRefreshToken_shouldRevokeCurrentAndCreateNew() {
        User user = new User();
        user.setUserId(1);

        RefreshToken current = new RefreshToken();
        current.setUser(user);
        current.setRevoked(false);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken spyNew = spy(new RefreshToken());
        spyNew.setUser(user);
        spyNew.setToken("new-token");

        RefreshTokenService spyService = Mockito.spy(service);
        doReturn(spyNew).when(spyService).createRefreshToken(user);

        RefreshToken result = spyService.rotateRefreshToken(current);

        assertTrue(current.getRevoked());
        assertEquals("new-token", result.getToken());
        verify(refreshTokenRepository).save(current);
    }

    @Test
    void deleteByUser_shouldDelegate() {
        User user = new User();
        service.deleteByUser(user);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void revokeAllUserTokens_shouldDelegate() {
        User user = new User();
        service.revokeAllUserTokens(user);
        verify(refreshTokenRepository).revokeAllUserTokens(user);
    }

    @Test
    void deleteExpiredTokens_shouldDelegate() {
        service.deleteExpiredTokens();
        verify(refreshTokenRepository).deleteByExpiryDateBefore(any(LocalDateTime.class));
    }
}