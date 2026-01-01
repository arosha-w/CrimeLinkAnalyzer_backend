package com.crimeLink.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPasswordResetRequest {
    
    @NotNull(message = "User ID is required")
    private Integer userId;
    
    @NotBlank(message = "New password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
        message = "Password must be at least 8 characters with 1 uppercase, 1 lowercase, 1 number, and 1 special character"
    )
    private String newPassword;
}
