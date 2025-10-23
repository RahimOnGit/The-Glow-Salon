package com.example.hairsalon.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ServiceRequest {

    @NotBlank(message = "Service name is required")
    @Size(min = 2, max = 100, message = "Service name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Duration is required")
    @Min(value = 5, message = "Duration must be at least 5 minutes")
    @Max(value = 480, message = "Duration cannot exceed 480 minutes (8 hours)")
    private Integer duration;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "10000.0", message = "Price cannot exceed 10000")
    private Double price;
}