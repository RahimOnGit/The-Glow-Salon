package com.example.hairsalon.repository;

import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.AppointmentStatus;
import com.example.hairsalon.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByEmployeeAndDate(Employee employee, LocalDate date);

    List<Appointment> findByUser_UserIdAndStatus(Long userId, AppointmentStatus status);
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.user.userId = :userId AND a.status = :status AND a.date >= :date")
    long countByUserUserIdAndStatusAndDateGreaterThanEqual(@Param("userId") Long userId, @Param("status") AppointmentStatus status, @Param("date") LocalDate date);

}