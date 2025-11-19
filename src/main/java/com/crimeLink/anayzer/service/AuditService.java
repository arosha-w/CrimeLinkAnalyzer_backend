package com.crimeLink.anayzer.service;

import com.crimeLink.anayzer.entity.LoginAudit;
import com.crimeLink.anayzer.repository.LoginAuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    @Autowired
    private LoginAuditRepository loginAuditRepository;

    public void logLoginAttempt(Integer userId, String email, boolean success, 
                                String failureReason, HttpServletRequest request) {
        LoginAudit audit = new LoginAudit();
        audit.setUserId(userId);
        audit.setEmail(email);
        audit.setSuccess(success);
        audit.setFailureReason(failureReason);
        audit.setIpAddress(getClientIP(request));
        audit.setUserAgent(request.getHeader("User-Agent"));
        audit.setLoginTime(LocalDateTime.now());
        
        loginAuditRepository.save(audit);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    public long countFailedLoginAttempts(String email, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return loginAuditRepository.countBySuccessFalseAndEmailAndLoginTimeAfter(email, since);
    }
}
