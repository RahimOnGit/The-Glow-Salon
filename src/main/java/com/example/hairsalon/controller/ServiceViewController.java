package com.example.hairsalon.controller;

import com.example.hairsalon.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

public class ServiceViewController {

    @Autowired
    private ServiceService serviceService;
    // This should return the main template that contains the fragments
    @GetMapping("/booking-fragment")
    public String showBookingForm() {
        return "services"; // or whatever your main template name is
    }

    @GetMapping("/booking-form-fragment")
    public String getBookingFormFragment(Model model) {
        // Add any necessary model attributes (list of locations, default date)
        return "fragments/service-list :: booking-form"; //  your booking form is defined here
    }

    // Endpoint for service list fragment
    @GetMapping("/service-list-fragment")
    public String getServiceListFragment(Model model) {
        model.addAttribute("services", serviceService.getAllServices());
        return "fragments/service-list :: service-list";
    }
}
