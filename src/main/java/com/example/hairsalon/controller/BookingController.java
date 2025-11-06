package com.example.hairsalon.controller;

import com.example.hairsalon.entity.Service;
import com.example.hairsalon.service.LocationService;
import com.example.hairsalon.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/salon")
public class BookingController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private ServiceService serviceService;

    @GetMapping("/booking")
    public String booking(Model model, @RequestParam String serviceIds) {
        List<Long> ids = Arrays.stream(serviceIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        model.addAttribute("serviceIds", ids);
        model.addAttribute("locations", locationService.getAllLocations());
        List<Service> selectedServices = ids.stream()
                .map(id -> serviceService.getServiceById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        model.addAttribute("selectedServices", selectedServices);
        int totalDuration = selectedServices.stream().mapToInt(s -> s.getDuration()).sum();
        double totalPrice = selectedServices.stream().mapToDouble(s -> s.getPrice()).sum();
        model.addAttribute("totalDuration", totalDuration);
        model.addAttribute("totalPrice", totalPrice);
        return "booking";
    }
}