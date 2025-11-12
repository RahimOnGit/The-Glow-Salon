package com.example.hairsalon.controller;

import com.example.hairsalon.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/salon")
public class BookingController {

    @Autowired
    private LocationService locationService;

    @PreAuthorize("hasRole(customer)")
    @GetMapping("/booking")
    public String booking(Model model, @RequestParam Long serviceId) {
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("locations", locationService.getAllLocations());
        return "booking";
    }
}