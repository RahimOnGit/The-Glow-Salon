package com.example.hairsalon.controller;

import com.example.hairsalon.entity.User;
import com.example.hairsalon.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    private final UserService userService;  // Inject UserService

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "logout", required = false) String logout, Model model) {
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        return "login";
    }

    //handle login success
    @PostMapping("/login-success")
    public String loginSuccess() {
        return """
    <div hx-redirect="/dashboard"> </div>
    <script> window.location.href= '/dashboard';</script>
    """;

    }

    // Handle login errors - return just the form fragment
    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }

    // Dashboard page
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        String email = auth.getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

//        if (userOpt.isPresent()) {
//            model.addAttribute("user", userOpt.get());
//        } else {
//            // Log error and redirect to login
//            model.addAttribute("error", "User data not found. Please log in again.");
//            return "redirect:/login";
//        }
//        return "dashboard";
//    }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            // Check role and return appropriate dashboard
            if ("admin".equals(user.getRole())) {
                return "adminDashboard";  // Serves adminDashboard.html
            } else {
                return "dashboard";  // Serves dashboard.html for other users
            }
        } else {
            // Log error and redirect to login
            model.addAttribute("error", "User data not found. Please log in again.");
            return "redirect:/login";
        }
    }

}