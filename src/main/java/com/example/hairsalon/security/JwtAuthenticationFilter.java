package com.example.hairsalon.security;

import com.example.hairsalon.entity.User;
import com.example.hairsalon.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import jakarta.servlet.http.Cookie; // Add this import
import java.util.Collections;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("Processing request in JwtAuthenticationFilter for URI: {}", request.getRequestURI());
        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                logger.debug("JWT found in request: {}", jwt);
                String email = jwtUtils.getEmailFromToken(jwt);
                Long userId = jwtUtils.getUserIdFromToken(jwt);
                logger.debug("Extracted from JWT - Email: {}, UserId: {}", email, userId);

                Optional<User> userOpt = userRepository.findById(userId);


                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    logger.debug("User found in DB for UserId {}: {}", userId, user.getEmail());

                    // validate token with user details
                    if (jwtUtils.validateToken(jwt, user)) {
                        logger.debug("JWT token validated successfully for user: {}", user.getEmail());

                        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPassword(),
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()))
                        );
                        logger.debug("User details loaded with authorities: {}", userDetails.getAuthorities());

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("User authenticated and SecurityContextHolder updated for: {}", email);
                    } else {
                        logger.warn("JWT token validation failed for user: {}", user.getEmail());
                    }
                } else {
                    logger.warn("User not found in DB for UserId {} extracted from JWT.", userId);
                }
            } else {
                logger.debug("No JWT found in request header.");
            }
        } catch (Exception e) {
            logger.error("JWT processing failed: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }


    //  Extracts the JWT from the Authorization header (Bearer token scheme).

    private String parseJwt(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    logger.debug("JWT found in cookie: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        logger.debug("No JWT cookie found in request.");
        return null;
    }
}
