package com.example.hairsalon.controller;

import com.example.hairsalon.dto.BlockDateRequest;
import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.StylistAvailability;
import com.example.hairsalon.service.EmployeeService;
import com.example.hairsalon.service.StylistAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/stylist/availability")
@RequiredArgsConstructor
public class StylistAvailabilityController {

    private final StylistAvailabilityService availabilityService;
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<StylistAvailability>> getMyBlockedDates(Authentication auth) {
        Employee stylist = employeeService.getCurrentStylist(auth);
        return ResponseEntity.ok(availabilityService.getByEmployee(stylist));
    }

    @PostMapping("/block")
    public ResponseEntity<StylistAvailability> blockDate(
            @RequestBody BlockDateRequest request,
            Authentication auth) {

        Employee stylist = employeeService.getCurrentStylist(auth);
        StylistAvailability blocked = availabilityService.blockDate(stylist, request);
        return ResponseEntity.ok(blocked);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unblockDate(@PathVariable Long id, Authentication auth) {
        Employee stylist = employeeService.getCurrentStylist(auth);
        availabilityService.unblockDate(stylist, id);
        return ResponseEntity.noContent().build();
    }
}