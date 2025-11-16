package com.crimeLink.anayzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
}
