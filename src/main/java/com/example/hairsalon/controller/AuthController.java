package com.example.hairsalon.controller;

import com.example.hairsalon.dto.RegisterRequest;
import com.example.hairsalon.entity.User;
import com.example.hairsalon.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

//    @PostMapping("/register")
//    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
//        User user = authService.register(request);
//        return ResponseEntity.ok(user);
//    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            // Returning a JSON error body for the frontend JS to display
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password must be provided."));

        }

        try {
            // Calls the AuthService to validate credentials and generate JWT
            String token = authService.login(email, password);
            // Create an HttpOnly cookie for the JWT
            ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(false) // Set to true in production with HTTPS
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 days
            .build();

            // Return success message with the cookie
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(Map.of("message", "Login successful"));

        } catch (RuntimeException e) {
            // Catch authentication failure (e.g., Invalid email or password)
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), error.getDefaultMessage());
            }
        });
        return ResponseEntity.badRequest().body(errors);
    }
}