package com.deepak.paymentService.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // =========================================================
        // 0. Actuator endpoints ko JWT filter se bypass karo
        // =========================================================
        String path = request.getRequestURI();

        if (path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // =========================================================
        // 1. Authorization header
        // =========================================================
        String authHeader =
                request.getHeader("Authorization");

        // JWT nahi mila
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // =========================================================
        // 2. Remove "Bearer "
        // =========================================================
        String token = authHeader.substring(7);

        try {

            // =====================================================
            // 3. JWT se email
            // =====================================================
            String email =
                    jwtService.extractEmail(token);

            // =====================================================
            // 4. JWT se role
            // =====================================================
            String role =
                    jwtService.extractRole(token);

            // =====================================================
            // 5. Authentication create karo
            // =====================================================
            if (email != null &&
                    role != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(authority)
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // =================================================
                // 6. SecurityContext me authentication set
                // =================================================
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception e) {

            System.out.println(
                    "Invalid JWT: " + e.getMessage()
            );
        }

        // =========================================================
        // 7. Request ko continue karo
        // =========================================================
        filterChain.doFilter(request, response);
    }
}