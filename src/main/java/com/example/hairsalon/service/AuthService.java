package com.example.hairsalon.service;


import com.example.hairsalon.dto.RegisterRequest;
import com.example.hairsalon.entity.User;
import com.example.hairsalon.repository.UserRepository;
import com.example.hairsalon.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;
    public User register(RegisterRequest request) {
        logger.debug("Attempting registration for email: {}", request.getEmail());

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            logger.warn("Registration failed: Email already exists {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        // visitCount defaults to 0 in entity

        // Validate and set role: must be one of 'admin', 'employee', 'customer'. Default to 'customer'
        String role = request.getRole();
        if (role == null || (!role.equals("admin") && !role.equals("employee") && !role.equals("customer"))) {
            role = "customer";
        }
        user.setRole(role);

        // visitCount defaults to 0 in entity

        User savedUser = userRepository.save(user);
        logger.debug("User registered successfully: {}", savedUser.getEmail());

        // Optionally increment visit count on registration (e.g., first visit)
        savedUser.setVisitCount(1);
        userRepository.save(savedUser);



//        User savedUser = userRepository.save(user);
//        logger.debug("User registered successfully: {}", savedUser.getEmail());
//
//        // Optionally increment visit count on registration (e.g., first visit)
//        savedUser.setVisitCount(1);
//        userRepository.save(savedUser);

        return savedUser;
    }

    public String login(String email, String password) {
        logger.debug("Attempting login for email: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("Login failed: User not found for email {}", email);
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOpt.get();
        logger.debug("User found: {}. Verifying password.", user.getEmail());
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Login failed: Password mismatch for user {}", email);
            throw new RuntimeException("Invalid email or password");
        }
        logger.debug("Password matched for user {}.", email);


        user.setVisitCount(user.getVisitCount() + 1);
        userRepository.save(user);


        return jwtUtils.generateToken(user);
    }

}


