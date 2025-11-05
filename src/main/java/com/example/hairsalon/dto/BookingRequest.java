package com.example.hairsalon.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequest {
    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    private LocalDate date;

    @NotNull(message = "Time is required")
    private LocalTime time;
}