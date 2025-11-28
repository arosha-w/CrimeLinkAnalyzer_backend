package com.crimeLink.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private Integer userId;
    private String userName;
    private String email;
    private String action;
    private String ipAddress;
    private String loginTime;
    private String logoutTime;
    private Boolean success;
}
