package com.example.hairsalon.repository;

import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.StylistAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StylistAvailabilityRepository extends JpaRepository<StylistAvailability, Long> {
    List<StylistAvailability> findByEmployeeOrderByDateAsc(Employee employee);
    boolean existsByEmployeeAndDate(Employee employee, LocalDate date);
}