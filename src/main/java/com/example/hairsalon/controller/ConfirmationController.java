package com.example.hairsalon.controller;

import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.service.AppointmentService;
import com.example.hairsalon.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class ConfirmationController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    public ConfirmationController(AppointmentService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    @GetMapping("/confirmation")
    public String confirmation(@RequestParam Long appointmentId, Model model) {  // Single appointment ID
        if (appointmentId == null) {
            return "redirect:/dashboard";
        }
        Optional<Appointment> optAppointment = appointmentService.getAppointmentById(appointmentId);
        if (optAppointment.isEmpty()) {
            model.addAttribute("error", "Appointment not found");
            return "confirmation";
        }
        Appointment appointment = optAppointment.get();

        // Load details: services, employee name, total price, etc.
        model.addAttribute("appointment", appointment);
        model.addAttribute("services", appointment.getServices());  // List of services
        model.addAttribute("stylistName", appointment.getEmployee().getFirstName() + " " + appointment.getEmployee().getLastName());
        model.addAttribute("totalPrice", appointment.getTotalPrice());
        model.addAttribute("totalDuration", appointment.getTotalDuration());
        model.addAttribute("date", appointment.getDate());
        model.addAttribute("time", appointment.getTime());
        model.addAttribute("location", appointment.getLocation().getName());

        return "confirmation";
    }
}