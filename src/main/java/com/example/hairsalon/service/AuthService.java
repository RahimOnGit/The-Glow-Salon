package com.example.hairsalon.service;


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


