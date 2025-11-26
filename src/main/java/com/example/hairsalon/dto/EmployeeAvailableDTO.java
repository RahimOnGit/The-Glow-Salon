package com.example.hairsalon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAvailableDTO {
    private Long employeeId;
    private String firstName;
    private String lastName;
    private Double averageRating;
}