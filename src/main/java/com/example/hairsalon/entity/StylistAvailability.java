package com.example.hairsalon.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "stylist_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StylistAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityType type = AvailabilityType.BLOCKED;

    public enum AvailabilityType {
        BLOCKED,
        PARTIAL
    }
}