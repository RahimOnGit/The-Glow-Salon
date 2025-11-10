package com.example.hairsalon.controller;

import com.example.hairsalon.dto.BookingRequest;
import com.example.hairsalon.entity.Employee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.Location;
import com.example.hairsalon.entity.Service;
import com.example.hairsalon.service.AppointmentService;
import com.example.hairsalon.service.EmployeeService;
import com.example.hairsalon.service.LocationService;
import com.example.hairsalon.service.ServiceService;
import com.example.hairsalon.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private UserService userService;

    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> bookAppointment(@Valid @RequestBody BookingRequest request, Authentication auth) {
        try {
            // Book the appointment and return success with ID
            Appointment appointment = appointmentService.bookAppointment(request);
            return ResponseEntity.ok(Map.of("message", "Appointment booked successfully", "appointmentId", appointment.getAppointmentId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments(Authentication auth) {
        String email = auth.getName();
        var user = userService.getUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        List<Appointment> appointments = appointmentService.getMyAppointments(user.getUserId());
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/available-employees")
    public ResponseEntity<List<Employee>> getAvailableEmployees(
            @RequestParam Long locationId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime time,
            @RequestParam String serviceIds) {  // e.g., "1,2,3"

        List<Long> ids = Arrays.stream(serviceIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        int totalDuration = ids.stream()
                .map(serviceService::getServiceById)
                .filter(java.util.Optional::isPresent)
                .mapToInt(s -> s.get().getDuration())
                .sum();

        var location = locationService.getLocationById(locationId);
        List<Employee> available = employeeService.getAvailableEmployees(location, date, time, totalDuration);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/locations")
    public ResponseEntity<?> getLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

}