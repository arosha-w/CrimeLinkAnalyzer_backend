package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService service;

    @Mock
    private UserRepository userRepository;

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setPasswordHash("pw");
        user.setRole("Admin");
        user.setStatus("Active");

        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("user@test.com");

        assertEquals("user@test.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrow_whenNotFound() {
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@test.com"));
    }
}