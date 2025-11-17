package com.example.hairsalon.repository;

import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.AppointmentStatus;
import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.User;
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

    // Updated: Custom query to sort by date ASC, then time ASC for full chronological order
    @Query("SELECT a FROM Appointment a WHERE a.user = :user ORDER BY a.date ASC, a.time ASC")
    List<Appointment> findByUserSortedByDateAndTime(@Param("user") User user);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.user.userId = :userId AND a.status = :status AND a.date >= :date")
    long countByUserUserIdAndStatusAndDateGreaterThanEqual(@Param("userId") Long userId, @Param("status") AppointmentStatus status, @Param("date") LocalDate date);

    @Query("""
        SELECT a FROM Appointment a
        WHERE (:userName IS NULL OR LOWER(CONCAT(a.user.firstName, ' ', a.user.lastName)) 
               LIKE LOWER(CONCAT('%', :userName, '%')))
          AND (:employeeName IS NULL OR LOWER(CONCAT(a.employee.firstName, ' ', a.employee.lastName)) 
               LIKE LOWER(CONCAT('%', :employeeName, '%')))
          AND (:status IS NULL OR a.status = :status)
          AND (:fromDate IS NULL OR a.date >= :fromDate)
          AND (:toDate IS NULL OR a.date <= :toDate)
        ORDER BY a.date DESC, a.time DESC
    """)
    List<Appointment> findFilteredAppointments(
            @Param("userName") String userName,
            @Param("employeeName") String employeeName,
            @Param("status") AppointmentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("SELECT a FROM Appointment a WHERE a.status = :status AND a.date < :date")
    List<Appointment> findByStatusAndDateBefore(@Param("status") AppointmentStatus status, @Param("date") LocalDate date);

}