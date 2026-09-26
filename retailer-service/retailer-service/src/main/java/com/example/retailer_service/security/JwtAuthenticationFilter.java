package com.example.retailer_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService) {

        this.jwtService = jwtService;
    }

    // =====================================================
    // SWAGGER REQUESTS SKIP
    // =====================================================

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String path =
                request.getServletPath();

        return path.startsWith("/swagger-ui/")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/v3/api-docs");
    }

    // =====================================================
    // JWT FILTER
    // =====================================================

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        // =================================================
        // No token
        // =================================================

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authHeader.substring(7).trim();

        // =================================================
        // IMPORTANT:
        // Only JWT validation should be inside try/catch
        // =================================================

        try {

            // Validate JWT

            if (!jwtService.isValid(token)) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.getWriter().write(
                        "Invalid or expired JWT token"
                );

                return;
            }

            // Extract username

            String username =
                    jwtService.extractUsername(token);

            // Extract role

            String role =
                    jwtService.extractRole(token);

            // Validate username

            if (username == null ||
                    username.isBlank()) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.getWriter().write(
                        "Username missing in JWT"
                );

                return;
            }

            // Validate role

            if (role == null ||
                    role.isBlank()) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.getWriter().write(
                        "Role missing in JWT"
                );

                return;
            }

            // Create authority

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_"
                                    + role.toUpperCase()
                    );

            // Create Authentication

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            token,
                            List.of(authority)
                    );

            // Store authentication

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

        } catch (Exception e) {

            SecurityContextHolder
                    .clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().write(
                    "Invalid or expired JWT token"
            );

            return;
        }

        // =================================================
        // VERY IMPORTANT
        //
        // filterChain OUTSIDE try/catch
        //
        // Business exceptions should NOT become 401
        // =================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}