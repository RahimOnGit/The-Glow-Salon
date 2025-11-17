package com.example.hairsalon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/services")
    public String adminServices() {
        return "admin/adminServices";
    }

    @GetMapping("/bookings")
    public String adminBookings() {
        return "admin/adminBookings";
    }

    @GetMapping("/users")
    public String adminUsers() {
        return "admin/adminUsers";  //for users page
    }
}