package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.RefreshToken;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.RefreshTokenRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDuration;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public RefreshToken createRefreshToken(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return createRefreshToken(user);
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenDuration / 1000));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public Optional<RefreshToken> findValidToken(String token) {
        Optional<RefreshToken> existing = refreshTokenRepository.findByTokenWithUser(token);
        if (existing.isEmpty()) return Optional.empty();

        RefreshToken rt = existing.get();
        if (rt.isExpired()) {
            refreshTokenRepository.delete(rt);
            return Optional.empty();
        }

        if (Boolean.TRUE.equals(rt.getRevoked())) {
            return Optional.empty();
        }

        return Optional.of(rt);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken currentToken) {
        currentToken.setRevoked(true);
        refreshTokenRepository.save(currentToken);

        return createRefreshToken(currentToken.getUser());
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
