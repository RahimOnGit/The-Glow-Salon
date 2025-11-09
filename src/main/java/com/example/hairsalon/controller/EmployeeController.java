package com.example.hairsalon.controller;

import com.example.hairsalon.entity.User;
import com.example.hairsalon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployeeController {

    @Autowired
    private UserService userService;

    @GetMapping("/employee/dashboard")
    public String dashboard(Model model) {
        //get and check the auth
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        //get the auth email and get the user by the email
        String email = auth.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        //add the user to  attribute stylist
        model.addAttribute("stylist", user);
        return "Stylist/employeeDashboard";
    }

}
