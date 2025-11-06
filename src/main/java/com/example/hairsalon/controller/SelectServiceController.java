package com.example.hairsalon.controller;

import com.example.hairsalon.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/salon")
public class SelectServiceController {

    @Autowired
    private ServiceService serviceService;

    @PostMapping("/select-service")
    public String selectService(@RequestParam("serviceIds") List<Long> serviceIds) {
        if (serviceIds.isEmpty()) {
            return "redirect:/salon/services?error=No service selected";
        }
        String idsStr = serviceIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        return "redirect:/salon/booking?serviceIds=" + idsStr;
    }
}