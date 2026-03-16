package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.LoginAudit;
import com.crimeLink.analyzer.repository.LoginAuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private LoginAuditRepository loginAuditRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuditService service;

    @Test
    void logLoginAttempt_shouldUseForwardedIp_whenPresent() {
        ReflectionTestUtils.setField(service, "loginAuditRepository", loginAuditRepository);

        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");
        when(loginAuditRepository.save(any(LoginAudit.class))).thenAnswer(inv -> inv.getArgument(0));

        service.logLoginAttempt(1, "u@test.com", false, "bad password", request);

        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(loginAuditRepository).save(captor.capture());

        LoginAudit saved = captor.getValue();
        assertEquals("10.0.0.1", saved.getIpAddress());
        assertEquals("JUnit", saved.getUserAgent());
        assertEquals("u@test.com", saved.getEmail());
        assertEquals(false, saved.getSuccess());
    }

    @Test
    void logLoginAttempt_shouldUseRemoteAddr_whenNoForwardedHeader() {
        ReflectionTestUtils.setField(service, "loginAuditRepository", loginAuditRepository);

        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn("JUnit");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        service.logLoginAttempt(1, "u@test.com", true, null, request);

        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(loginAuditRepository).save(captor.capture());

        assertEquals("127.0.0.1", captor.getValue().getIpAddress());
    }

    @Test
    void countFailedLoginAttempts_shouldDelegateToRepository() {
        ReflectionTestUtils.setField(service, "loginAuditRepository", loginAuditRepository);

        when(loginAuditRepository.countBySuccessFalseAndEmailAndLoginTimeAfter(eq("u@test.com"), any(LocalDateTime.class)))
                .thenReturn(3L);

        long count = service.countFailedLoginAttempts("u@test.com", 15);

        assertEquals(3L, count);
    }
}