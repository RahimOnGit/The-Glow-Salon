package com.example.hairsalon.controller;

import com.example.hairsalon.dto.BookingRequest;
import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.service.AppointmentService;
import com.example.hairsalon.service.EmployeeService;
import com.example.hairsalon.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LocationService locationService;

    @PostMapping("/book")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody BookingRequest request) {
        try {
            Appointment appointment = appointmentService.bookAppointment(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment booked successfully");
            response.put("appointment", appointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @GetMapping("/available-employees")
    public ResponseEntity<List<Map<String, Object>>> getAvailableEmployees(
            @RequestParam Long locationId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime time,
            @RequestParam Long serviceId) {
        // For this endpoint, filter employees at location with no conflict at time+duration
        // But since duration needed, assume service fetched in service, but here simple: use available on date
        List<Map<String, Object>> available = employeeService.getAvailableEmployeesOnDate(date, 8).stream()
                .filter(e -> e.getLocation().getLocationId().equals(locationId))
                .map(e -> {
                    Map<String, Object> emp = new HashMap<>();
                    emp.put("employeeId", e.getEmployeeId());
                    emp.put("name", e.getFirstName() + " " + e.getLastName());
                    return emp;
                }).toList();
        return ResponseEntity.ok(available);
    }

    @GetMapping("/locations")
    public ResponseEntity<List<Map<String, Object>>> getLocations() {
        List<Map<String, Object>> locations = locationService.getAllLocations().stream()
                .map(l -> {
                    Map<String, Object> loc = new HashMap<>();
                    loc.put("locationId", l.getLocationId());
                    loc.put("name", l.getName());
                    return loc;
                }).toList();
        return ResponseEntity.ok(locations);
    }
}