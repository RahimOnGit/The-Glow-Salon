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
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Autowired
    private UserService userService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ServiceService serviceService;
    @Autowired
    private LocationService locationService;

    private static final long MAX_BOOKINGS_PER_DAY = 8;

    @Transactional
    public Appointment bookAppointment(BookingRequest request) {
        try {
            // Get current user from security context
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            // Fetch multiple services
            List<Service> services = request.getServiceIds().stream()
                    .map(serviceId -> serviceService.getServiceById(serviceId)
                            .orElseThrow(() -> new RuntimeException("Service not found: " + serviceId)))
                    .collect(Collectors.toList());
            if (services.isEmpty()) {
                throw new RuntimeException("At least one service must be selected");
            }
            Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
            Location location = locationService.getLocationById(request.getLocationId());
            // Calculate total duration for conflict check
            int totalDuration = services.stream().mapToInt(Service::getDuration).sum();
            // Conflict check with total duration
            if (hasTimeConflict(employee, request.getDate(), request.getTime(), totalDuration)) {
                throw new RuntimeException("Time slot conflict with existing appointment");
            }
            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setUser(user);
            appointment.setEmployee(employee);
            appointment.setServices(services);
            appointment.setLocation(location);
            appointment.setDate(request.getDate());
            appointment.setTime(request.getTime());
            appointment.setStatus(AppointmentStatus.PENDING);
            Appointment saved = appointmentRepository.save(appointment);
            appointmentRepository.flush();
            // Increment visit count
            userService.incrementVisitCount(user.getUserId());
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Failed to book appointment: " + e.getMessage(), e);
        }
    }

    public List<Appointment> getMyAppointments(Long userId) {
        User user = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return appointmentRepository.findByUserSortedByDateAndTime(user);
    }

    @Transactional
    public void cancelAppointment(Long userId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appointment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to cancel this appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel this appointment");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.saveAndFlush(appointment);
    }

    public int getPendingUpcomingCount(Long userId) {
        LocalDate today = LocalDate.now();
        return (int) appointmentRepository.countByUserUserIdAndStatusAndDateGreaterThanEqual(userId, AppointmentStatus.PENDING, today);
    }

    private boolean hasTimeConflict(Employee employee, LocalDate date, LocalTime startTime, int duration) {
        List<Appointment> existing = appointmentRepository.findByEmployeeAndDate(employee, date);
        LocalTime endTime = startTime.plusMinutes(duration);
        for (Appointment a : existing) {
            if (!AppointmentStatus.PENDING.equals(a.getStatus())) continue;
            LocalTime aEndTime = a.getTime().plusMinutes(a.getTotalDuration());
            if (startTime.isBefore(aEndTime) && endTime.isAfter(a.getTime())) {
                return true;
            }
        }
        return false;
    }
}