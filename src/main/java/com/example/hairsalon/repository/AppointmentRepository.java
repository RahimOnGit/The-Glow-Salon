package com.example.hairsalon.repository;

import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByEmployeeAndDate(Employee employee, LocalDate date);

    List<Appointment> findByUser_UserIdAndStatus(Long userId, String status);
}