package com.example.hairsalon.controller;

import com.example.hairsalon.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/salon")
public class SelectServiceController {

    @Autowired
    private ServiceService serviceService;

    @PostMapping("/select-service")
    public String selectService(@RequestParam("serviceId") Long serviceId) {
        if (!serviceService.existsById(serviceId)) {
            // Handle error, perhaps redirect with error
            return "redirect:/salon/services?error=Service not found";
        }
        return "redirect:/salon/booking?serviceId=" + serviceId;
    }
}