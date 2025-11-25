package com.example.hairsalon.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record BlockDateRequest(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String reason
) {}