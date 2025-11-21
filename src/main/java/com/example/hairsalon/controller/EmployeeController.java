package com.example.hairsalon.controller;

import com.example.hairsalon.dto.AppointmentDto;
import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.User;
import com.example.hairsalon.service.AppointmentService;
import com.example.hairsalon.service.EmployeeService;
import com.example.hairsalon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeFormatter;
import java.util.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
public class EmployeeController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/employee/dashboard")
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        try {
            // Get and check the auth
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                return "login";
            }

            // Get the auth email and get the user by the email(logged-in employee)
            String email = auth.getName();
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            System.out.println("Found user: " + user.getEmail() + " with ID: " + user.getUserId());

            // Find the employee for this user
            Employee employee = employeeService.getEmployeeByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Employee not found for user ID: " + user.getUserId() + " (" + user.getEmail() + ")"));

            System.out.println("Found employee: " + employee.getFirstName() + " " + employee.getLastName());

            // Use the provided date or default to today
            LocalDate appointmentDate = (date != null) ? date : LocalDate.now();

            // Get appointments for the selected date for this employee
            var appointments = appointmentService.getAppointmentsByEmployeeAndDate(employee, appointmentDate)
                    .stream()
                    .sorted(Comparator.comparing(Appointment::getTime))
                    .map(appt -> new com.example.hairsalon.dto.AppointmentDto(
                            appt.getAppointmentId(),
                            appt.getUser().getFirstName() + " " + appt.getUser().getLastName(),
                            appt.getTime(),
                            appt.getTotalDuration(),
                            appt.getServices().stream().map(com.example.hairsalon.entity.Service::getName).collect(Collectors.toList()),
                            appt.getLocation().getName(),
                            appt.getStatus()
                    ))
                    .collect(Collectors.toList());

            // Format the title based on the date
            String title;
            if (appointmentDate.equals(LocalDate.now())) {
                title = "Today's Appointments";
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
                title = appointmentDate.format(formatter) + " Appointments";
            }

            // Add attributes to model
            model.addAttribute("stylist", user);
            model.addAttribute("appointments", appointments);
            model.addAttribute("title", title);

            return "Stylist/employeeDashboard";

        } catch (Exception e) {
            System.err.println("Error in employee dashboard: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    @GetMapping("/api/employee/appointments")
    @ResponseBody
    public ResponseEntity<List<AppointmentDto>> getAppointmentsForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        try {
            String email = auth.getName();
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            Employee employee = employeeService.getEmployeeByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Employee not found for user ID: " + user.getUserId()));

            List<AppointmentDto> appointments = appointmentService.getAppointmentsByEmployeeAndDate(employee, date)
                    .stream()
                    .sorted(Comparator.comparing(Appointment::getTime))
                    .map(appt -> new AppointmentDto(
                            appt.getAppointmentId(),
                            appt.getUser().getFirstName() + " " + appt.getUser().getLastName(),
                            appt.getTime(),
                            appt.getTotalDuration(),
                            appt.getServices().stream().map(com.example.hairsalon.entity.Service::getName).collect(Collectors.toList()),
                            appt.getLocation().getName(),
                            appt.getStatus()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}