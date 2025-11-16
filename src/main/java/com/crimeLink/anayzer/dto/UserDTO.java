package com.crimeLink.anayzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer userId;
    private String name;
    private LocalDate dob;
    private String gender;
    private String address;
    private String role;
    private String badgeNo;
    private String email;
    private String status;
}
