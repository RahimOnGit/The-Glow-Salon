package com.example.hairsalon.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LogoutController {

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        // Clear JWT cookie if exists
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0); // Immediately expire
        response.addCookie(jwtCookie);

        // Clear Security Context
        SecurityContextHolder.clearContext();

        return "redirect:/login?logout";
    }
}