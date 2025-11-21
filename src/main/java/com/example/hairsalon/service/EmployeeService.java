package com.example.hairsalon.service;

import com.example.hairsalon.entity.*;
import com.example.hairsalon.repository.AppointmentRepository;
import com.example.hairsalon.repository.EmployeeRepository;
import com.example.hairsalon.repository.StylistAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserService userService;

//    @Autowired
//    private StylistAvailabilityService stylistAvailabilityService;

    @Autowired
    private StylistAvailabilityRepository stylistAvailabilityRepository;

    public Optional<Employee> getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserUserId(userId);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
    }


    public List<Employee> getEmployeesByLocation(Location location) {
        return employeeRepository.findByLocation(location);
    }

    public List<Employee> getAvailableEmployeesOnDate(LocalDate date, long maxBookingsPerDay) {
        return employeeRepository.findAvailableOnDate(date, maxBookingsPerDay);
    }

    public List<Employee> getAvailableEmployees(Location location, LocalDate date, LocalTime startTime, int duration) {
        List<Employee> employees = getEmployeesByLocation(location);
        return employees.stream()
                .filter(e -> !hasTimeConflict(e, date, startTime, duration))
                .collect(Collectors.toList());
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

    public Employee getCurrentStylist(Authentication auth) {
        String email = auth.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return getEmployeeByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("You are not a stylist"));
    }


    public boolean isStylistBlockedOnDate(Employee employee, LocalDate date, LocalTime requestStart, int duration) {
        List<StylistAvailability> blocks = stylistAvailabilityRepository.findByEmployeeOrderByDateAsc(employee);

        for (StylistAvailability block : blocks) {
            if (!block.getDate().equals(date)) continue;

            if (block.getType() == StylistAvailability.AvailabilityType.BLOCKED) {
                return true;
            }

            if (block.getType() == StylistAvailability.AvailabilityType.PARTIAL) {
                LocalTime blockStart = block.getStartTime();
                LocalTime blockEnd = block.getEndTime();
                LocalTime requestEnd = requestStart.plusMinutes(duration);

                if (requestStart.isBefore(blockEnd) && requestEnd.isAfter(blockStart)) {
                    return true;
                }
            }
        }
        return false;
    }
}
