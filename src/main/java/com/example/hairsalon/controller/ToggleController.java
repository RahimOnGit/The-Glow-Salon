package com.example.hairsalon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ToggleController {

    @PostMapping("/toggle")
    @ResponseBody
    public String togglePanel() {
        // Return HTML that HTMX will swap in
        return """
            <div class="alert alert-success shadow-lg mt-4">
                <div>
                    <span>✅ Panel updated successfully at %s</span>
                </div>
            </div>
            """.formatted(java.time.LocalTime.now());
    }
}