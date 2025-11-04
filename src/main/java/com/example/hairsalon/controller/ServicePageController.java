package com.example.hairsalon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/salon")
public class ServicePageController {
@GetMapping("/services")
    public String getServicePage() {
    return "services";
}


    @GetMapping("booking")
    public String getBookingPage() {
        return "booking";
    }

}
