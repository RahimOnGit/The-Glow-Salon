package com.example.hairsalon.service;

import com.example.hairsalon.dto.BookingRequest;
import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.AppointmentStatus;
import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.Location;
import com.example.hairsalon.entity.Service;
import com.example.hairsalon.entity.User;
import com.example.hairsalon.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

//@Service
@org.springframework.stereotype.Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ServiceService serviceService; // Assuming ServiceService exists

    @Autowired
    private LocationService locationService;

    private static final long MAX_BOOKINGS_PER_DAY = 8; // Configurable

    public Appointment bookAppointment(BookingRequest request) {
        // Get current user from security context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        // Validate and fetch entities
        Service service = serviceService.getServiceById(request.getServiceId()).orElseThrow(() -> new RuntimeException("Service not found"));
        Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
        Location location = locationService.getLocationById(request.getLocationId());

        // Conflict check
        if (hasTimeConflict(employee, request.getDate(), request.getTime(), service.getDuration())) {
            throw new RuntimeException("Time slot conflict with existing appointment");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setEmployee(employee);
        appointment.setService(service);
        appointment.setLocation(location);
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        appointment.setStatus(AppointmentStatus.PENDING);

        // Save
        Appointment saved = appointmentRepository.save(appointment);

        // Optionally increment visit count
        userService.incrementVisitCount(user.getUserId());

        return saved;
    }

    private boolean hasTimeConflict(Employee employee, LocalDate date, LocalTime startTime, int duration) {
        List<Appointment> existing = appointmentRepository.findByEmployeeAndDate(employee, date);
        LocalTime endTime = startTime.plusMinutes(duration);

        for (Appointment a : existing) {
            if (!AppointmentStatus.PENDING.equals(a.getStatus())) continue; // Only check pending
            LocalTime aEndTime = a.getTime().plusMinutes(a.getService().getDuration());
            // Overlap if start < aEnd and end > aStart
            if (startTime.isBefore(aEndTime) && endTime.isAfter(a.getTime())) {
                return true;
            }
        }
        return false;
    }

    public List<Appointment> getUserAppointments(Long userId) {
        return appointmentRepository.findByUser_UserIdAndStatus(userId, AppointmentStatus.PENDING);
    }
}