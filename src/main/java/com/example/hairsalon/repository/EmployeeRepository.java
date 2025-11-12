package com.example.hairsalon.repository;

import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByLocation(Location location);

    @Query("SELECT e FROM Employee e LEFT JOIN e.appointments a ON (a.employee = e AND a.date = :date AND a.status = 'scheduled') GROUP BY e HAVING COUNT(a) < :maxBookings")
    List<Employee> findAvailableOnDate(@Param("date") LocalDate date, @Param("maxBookings") long maxBookings);


        // Fix this method - it should find by user.userId
        @Query("SELECT e FROM Employee e WHERE e.user.userId = :userId")
        Optional<Employee> findByUserUserId(@Param("userId") Long userId);


        @Query("SELECT e FROM Employee e WHERE SIZE(e.appointments) < :maxBookingsPerDay")
        List<Employee> findAvailableOnDate(@Param("maxBookingsPerDay") long maxBookingsPerDay);
    }

