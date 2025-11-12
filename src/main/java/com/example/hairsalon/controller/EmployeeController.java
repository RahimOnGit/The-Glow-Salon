package com.example.hairsalon.controller;

import com.example.hairsalon.entity.Employee;
import com.example.hairsalon.entity.User;
import com.example.hairsalon.service.AppointmentService;
import com.example.hairsalon.service.EmployeeService;
import com.example.hairsalon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
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
    public String dashboard(Model model) {
        try {
            // Get and check the auth
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                return "redirect:/login";
            }

            // Get the auth email and get the user by the email
            String email = auth.getName();
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            System.out.println("Found user: " + user.getEmail() + " with ID: " + user.getUserId());

            // Find the employee associated with this user
            Employee employee = employeeService.getEmployeeByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Employee not found for user ID: " + user.getUserId() + " (" + user.getEmail() + ")"));

            System.out.println("Found employee: " + employee.getFirstName() + " " + employee.getLastName());

            // Get today's appointments for this employee
            LocalDate today = LocalDate.now();
            var todayAppointments = appointmentService.getAppointmentsByEmployeeAndDate(employee, today)
                    .stream()
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

            // Add attributes to model
            model.addAttribute("stylist", user);
            model.addAttribute("appointments", todayAppointments);
            model.addAttribute("title", "Today's Appointments");

            return "Stylist/employeeDashboard";

        } catch (Exception e) {
            System.err.println("Error in employee dashboard: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}