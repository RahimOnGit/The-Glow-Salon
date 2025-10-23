package com.example.hairsalon.controller;

import com.example.hairsalon.entity.Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";  // This serves your index.html
    }

    @GetMapping("/temp-services-add")
    public String tempServiceForm(Model model) {
        model.addAttribute("service", new Service());
        return "add-service";
    }
}
