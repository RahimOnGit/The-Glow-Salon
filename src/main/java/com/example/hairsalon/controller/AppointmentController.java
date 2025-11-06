package com.example.hairsalon.controller;

import com.example.hairsalon.dto.BookingRequest;
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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
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

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody BookingRequest request) {
        try {
            List<Long> serviceIds = request.getServiceIds();

            // If serviceIds is null but serviceIdsString is provided, parse it
            if ((serviceIds == null || serviceIds.isEmpty()) && request.getServiceIdsString() != null && !request.getServiceIdsString().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    serviceIds = mapper.readValue(request.getServiceIdsString(), new TypeReference<List<Long>>() {});
                } catch (JsonProcessingException e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Invalid service IDs format: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            // Validate that at least one service is selected
            if (serviceIds == null || serviceIds.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "At least one service must be selected");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            request.setServiceIds(serviceIds);
            Appointment appointment = appointmentService.bookAppointment(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment booked successfully");
            response.put("appointmentId", appointment.getAppointmentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "CONFLICT");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/available-employees")
    public ResponseEntity<List<Map<String, Object>>> getAvailableEmployees(
            @RequestParam Long locationId,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam List<Long> serviceIds) {

        try {
            LocalDate parsedDate = LocalDate.parse(date);
            LocalTime parsedTime = LocalTime.parse(time);

            int totalDuration = serviceIds.stream()
                    .map(serviceId -> serviceService.getServiceById(serviceId)
                            .orElseThrow(() -> new RuntimeException("Service not found: " + serviceId)))
                    .mapToInt(Service::getDuration)
                    .sum();

            Location location = locationService.getLocationById(locationId);
            List<Map<String, Object>> available = employeeService.getAvailableEmployees(location, parsedDate, parsedTime, totalDuration).stream()
                    .map(e -> {
                        Map<String, Object> emp = new HashMap<>();
                        emp.put("employeeId", e.getEmployeeId());
                        emp.put("name", e.getFirstName() + " " + e.getLastName());
                        return emp;
                    }).collect(Collectors.toList());
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
