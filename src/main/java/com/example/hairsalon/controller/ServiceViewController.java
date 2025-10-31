package com.example.hairsalon.controller;

import com.example.hairsalon.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/service-view")
public class ServiceViewController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public String getServiceView(Model model) {
        model.addAttribute("services", serviceService.getAllServices());
    return "fragments/service-list";
    }
}
