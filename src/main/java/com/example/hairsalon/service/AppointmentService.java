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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private ServiceService serviceService;

    @Autowired
    private LocationService locationService;

    private static final long MAX_BOOKINGS_PER_DAY = 8; // Configurable

    public List<Appointment> bookAppointment(BookingRequest request) {
        // Get current user from security context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        // Validate and fetch entities
        Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
        Location location = locationService.getLocationById(request.getLocationId());
        LocalDate date = request.getDate();
        LocalTime currentTime = request.getTime();

        List<Appointment> appointments = new ArrayList<>();
        for (Long serviceId : request.getServiceIds()) {
            Service service = serviceService.getServiceById(serviceId).orElseThrow(() -> new RuntimeException("Service not found: " + serviceId));

            // Conflict check for this slot
            if (hasTimeConflict(employee, date, currentTime, service.getDuration())) {
                throw new RuntimeException("Time slot conflict with existing appointment for service: " + service.getName());
            }

            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setUser(user);
            appointment.setEmployee(employee);
            appointment.setService(service);
            appointment.setLocation(location);
            appointment.setDate(date);
            appointment.setTime(currentTime);
            appointment.setStatus(AppointmentStatus.PENDING);

            // Save
            appointments.add(appointmentRepository.save(appointment));

            // Advance time for next service
            currentTime = currentTime.plusMinutes(service.getDuration());
        }

        // Increment visit count once
        userService.incrementVisitCount(user.getUserId());

        return appointments;
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

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }
}