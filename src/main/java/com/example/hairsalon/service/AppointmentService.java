package com.example.hairsalon.service;

import com.example.hairsalon.dto.AppointmentAdminDTO;
import com.example.hairsalon.dto.BookingRequest;
import com.example.hairsalon.entity.*;
import com.example.hairsalon.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

// ✅ Viktigt: använd fullt kvalificerat namn för Spring-annotationen
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

    private static final long MAX_BOOKINGS_PER_DAY = 8;

    public List<AppointmentAdminDTO> getAppointmentsForAdmin(
            String customer,
            String stylist,
            AppointmentStatus status,
            LocalDate from,
            LocalDate to
    ) {
        List<Appointment> appointments = appointmentRepository.findFilteredAppointments(customer, stylist, status, from, to);

        return appointments.stream()
                .map(a -> new AppointmentAdminDTO(
                        a.getAppointmentId(),
                        a.getUser() != null ? a.getUser().getFirstName() + " " + a.getUser().getLastName() : "-",
                        a.getEmployee() != null ? a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName() : "-",
                        a.getServices() != null
                                ? a.getServices().stream()
                                // 👇 Pekar uttryckligen på din entityklass
                                .map(com.example.hairsalon.entity.Service::getName)
                                .collect(Collectors.joining(", "))
                                : "-",
                        a.getLocation() != null ? a.getLocation().getName() : "-",
                        a.getDate(),
                        a.getTime(),
                        a.getStatus() != null ? a.getStatus().name() : "-"
                ))
                .collect(Collectors.toList());
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Transactional
    public Appointment bookAppointment(BookingRequest request) {
        try {
            String email = org.springframework.security.core.context.SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 👇 Här också tydliggör vi vilken Service du menar
            List<com.example.hairsalon.entity.Service> services = request.getServiceIds().stream()
                    .map(serviceId -> serviceService.getServiceById(serviceId)
                            .orElseThrow(() -> new RuntimeException("Service not found: " + serviceId)))
                    .collect(Collectors.toList());

            if (services.isEmpty()) {
                throw new RuntimeException("At least one service must be selected");
            }

            Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
            Location location = locationService.getLocationById(request.getLocationId());

            int totalDuration = services.stream()
                    .mapToInt(com.example.hairsalon.entity.Service::getDuration)
                    .sum();

            if (hasTimeConflict(employee, request.getDate(), request.getTime(), totalDuration)) {
                throw new RuntimeException("Time slot conflict with existing appointment");
            }

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

            userService.incrementVisitCount(user.getUserId());

            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Failed to book appointment: " + e.getMessage(), e);
        }
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
