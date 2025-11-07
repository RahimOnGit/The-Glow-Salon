package com.example.hairsalon.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class BookingRequest {
    private List<Long> serviceIds;

    private String serviceIdsString; // Can be null if serviceIds is provided

    @NotNull
    private Long employeeId;

    @NotNull
    private Long locationId;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime time;
}
