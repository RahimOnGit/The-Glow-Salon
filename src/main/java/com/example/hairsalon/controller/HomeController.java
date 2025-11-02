package com.example.hairsalon.controller;

import com.example.hairsalon.entity.Service;
import com.example.hairsalon.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private ServiceService serviceService;
    @GetMapping("/")
    public String home(Model model) {

        List<Service> services = serviceService.getAllServices();
        model.addAttribute("services", services);
        return "index";  // This serves your index.html
    }
}
