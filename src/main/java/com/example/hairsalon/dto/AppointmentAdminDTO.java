package com.example.hairsalon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAdminDTO {

    private Long id;
    private String customerName;
    private String stylistName;
    private String serviceNames;
    private String location;
    private LocalDate date;
    private LocalTime time;
    private String status;
}
