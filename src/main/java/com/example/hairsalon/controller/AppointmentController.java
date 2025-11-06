package com.example.hairsalon.controller;

import com.example.hairsalon.dto.BookingRequest;
import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.Location;
import com.example.hairsalon.entity.Service;
import com.example.hairsalon.service.AppointmentService;
import com.example.hairsalon.service.EmployeeService;
import com.example.hairsalon.service.LocationService;
import com.example.hairsalon.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @PostMapping("/book")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody BookingRequest request) {
        try {
            List<Appointment> appointments = appointmentService.bookAppointment(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", appointments.size() > 1 ? "Appointments booked successfully" : "Appointment booked successfully");
            List<Long> appointmentIds = appointments.stream().map(Appointment::getAppointmentId).collect(Collectors.toList());
            response.put("appointmentIds", appointmentIds);
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
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam String serviceIds) {
        LocalDate parsedDate = LocalDate.parse(date);
        LocalTime parsedTime = LocalTime.parse(time);
        List<Long> ids = Arrays.stream(serviceIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        int totalDuration = 0;
        for (Long id : ids) {
            Service service = serviceService.getServiceById(id).orElseThrow(() -> new RuntimeException("Service not found"));
            totalDuration += service.getDuration();
        }
        Location location = locationService.getLocationById(locationId);
        List<Map<String, Object>> available = employeeService.getAvailableEmployees(location, parsedDate, parsedTime, totalDuration).stream()
                .map(e -> {
                    Map<String, Object> emp = new HashMap<>();
                    emp.put("employeeId", e.getEmployeeId());
                    emp.put("name", e.getFirstName() + " " + e.getLastName());
                    return emp;
                }).collect(Collectors.toList());
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