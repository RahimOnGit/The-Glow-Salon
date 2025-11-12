package com.example.hairsalon.dto;

import com.example.hairsalon.entity.AppointmentStatus;

import java.time.LocalTime;
import java.util.List;
public record AppointmentDto(
        Long id,
        String customerName,
        LocalTime startTime,
        int durationInMinutes,
        List<String> serviceNames,
        String location,
        AppointmentStatus status) {
}
