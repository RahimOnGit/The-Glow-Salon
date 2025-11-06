package com.example.hairsalon.controller;

import com.example.hairsalon.entity.Appointment;
import com.example.hairsalon.service.AppointmentService;
import com.example.hairsalon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ConfirmationController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    public ConfirmationController(AppointmentService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    @GetMapping("/confirmation")
    public String confirmation(@RequestParam(defaultValue = "") String appointmentIds, Model model) {
        if (appointmentIds.isEmpty()) {
            return "redirect:/dashboard";
        }
        List<Long> ids = Arrays.stream(appointmentIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        List<Appointment> appointments = new java.util.ArrayList<>();
        for (Long id : ids) {
            appointmentService.getAppointmentById(id).ifPresent(appointments::add);
        }
        appointments.sort(Comparator.comparing(Appointment::getTime));
        double totalPrice = appointments.stream().mapToDouble(a -> a.getService().getPrice()).sum();
        model.addAttribute("appointments", appointments);
        model.addAttribute("totalPrice", totalPrice);
        return "confirmation";
    }
}