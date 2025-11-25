package com.example.hairsalon.service;

import com.example.hairsalon.dto.AppointmentAdminDTO;
import com.example.hairsalon.dto.BookingRequest;
import com.example.hairsalon.entity.*;
import com.example.hairsalon.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private StylistAvailabilityService stylistAvailabilityService;


    private static final long MAX_BOOKINGS_PER_DAY = 8;

    public List<Appointment> getAppointmentsByEmployeeAndDate(Employee employee, LocalDate date) {
        return appointmentRepository.findByEmployeeAndDate(employee, date);
    }

    public Optional<Appointment> getAppointmentById(Long id)
    {
        return appointmentRepository.findById(id);
    }
    public List<Appointment> getMyAppointments(Long userId) {
        User user = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return appointmentRepository.findByUserSortedByDateAndTime(user);
    }


    public List<AppointmentAdminDTO> getAppointmentsForAdmin(
            String customer,
            String stylist,
            AppointmentStatus status,
            LocalDate from,
            LocalDate to
    ) {
        autoCancelOverduePendingAppointments();

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

    @Transactional
    public void updateAppointmentStatus(Long id, AppointmentStatus status) {
        Appointment appointment = getAppointmentById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(status);
        appointmentRepository.save(appointment);
    }




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
            if (employeeService.isStylistBlockedOnDate(employee, request.getDate(), request.getTime(), totalDuration)) {
                throw new RuntimeException("This stylist is not available on the selected date.");
            }

            if (hasTimeConflict(employee, request.getDate(), request.getTime(), totalDuration)) {
                throw new RuntimeException("This time slot is already booked. Please choose another time.");
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

    private boolean hasTimeConflict(Employee employee, LocalDate date, LocalTime startTime, int duration) {
        List<Appointment> existing = appointmentRepository.findByEmployeeAndDate(employee, date);
        LocalTime endTime = startTime.plusMinutes(duration);

        List<StylistAvailability> blocked = stylistAvailabilityService.getByEmployee(employee);
        for (StylistAvailability block : blocked) {
            if (!block.getDate().equals(date)) continue;

            if (block.getType() == StylistAvailability.AvailabilityType.BLOCKED) {
                return true;
            }

            if (block.getType() == StylistAvailability.AvailabilityType.PARTIAL) {
                LocalTime blockStart = block.getStartTime();
                LocalTime blockEnd = block.getEndTime();
                LocalTime requestEnd = startTime.plusMinutes(duration);

                if (startTime.isBefore(blockEnd) && requestEnd.isAfter(blockStart)) {
                    return true;
                }
            }
        }

        for (Appointment a : existing) {
            if (!AppointmentStatus.CONFIRMED.equals(a.getStatus())) continue;
            LocalTime aEndTime = a.getTime().plusMinutes(a.getTotalDuration());
            if (startTime.isBefore(aEndTime) && endTime.isAfter(a.getTime())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getDate(),
                appointment.getTime().plusMinutes(appointment.getTotalDuration())
        );

        if (now.isAfter(appointmentDateTime)) {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.saveAndFlush(appointment);
        } else {
            throw new RuntimeException("Time has not passed yet");
        }
    }

    public int getPendingUpcomingCount(Long userId) {
        LocalDate today = LocalDate.now();
        return (int) appointmentRepository.countByUserUserIdAndStatusAndDateGreaterThanEqual(userId, AppointmentStatus.PENDING, today);
    }


    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // Run daily at
    public void autoCancelOverduePendingAppointments() {
        LocalDate today = LocalDate.now();
        List<Appointment> overduePending = appointmentRepository.findByStatusAndDateBefore(AppointmentStatus.PENDING, today);
        int cancelledCount = 0;
        for (Appointment appointment : overduePending) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
            cancelledCount++;
        }
        if (cancelledCount > 0) {
            System.out.println("Auto-cancelled " + cancelledCount + " overdue pending appointments.");
        }
    }



}