package com.example.hairsalon.controller;

import com.example.hairsalon.service.LocationService;
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
    @Autowired
    private LocationService locationService;

    // Endpoint for service list fragment
    @GetMapping("/service-list-fragment")
    public String getServiceListFragment(Model model) {
        model.addAttribute("services", serviceService.getAllServices());
        model.addAttribute("locations", locationService.getAllLocations());
        return "fragments/service-list :: service-list";
    }

    // Endpoint for booking form fragment
    @GetMapping("/booking-form-fragment")
    public String getBookingFormFragment(Model model) {
        model.addAttribute("locations", locationService.getAllLocations());
        return "fragments/service-list :: booking-form";
    }

    // Endpoint for service view (if needed)
    @GetMapping("/service-view")
    public String getServiceView(Model model) {
        model.addAttribute("services", serviceService.getAllServices());
        model.addAttribute("locations", locationService.getAllLocations());
        return "fragments/service-list :: service-list";
    }


    @GetMapping("/booking-fragment")
    public String showBookingForm() {
        return "services"; // or whatever your main template name is
    }

}
