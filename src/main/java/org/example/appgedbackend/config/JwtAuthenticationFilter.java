package org.example.appgedbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.appgedbackend.Service.CustomUserDetailsService;
import org.example.appgedbackend.Service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();
        if (path.startsWith("/api/")) {
            System.out.println("🌐 API Request: " + method + " " + path);
        }

        System.out.println("=== 🛡️ JWT FILTER START ===");
        System.out.println("📨 Request: " + method + " " + path);

        // URLs publiques
        if (path.startsWith("/auth") || path.startsWith("/api/setup") || path.startsWith("/uploads")) {
            System.out.println("✅ Public route - skipping auth");
            filterChain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("✅ OPTIONS request - skipping auth");
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ No Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            String username = jwtService.extractUsername(jwt);
            String role = jwtService.extractRole(jwt);

            System.out.println("👤 User from token: " + username);
            System.out.println("🎯 Role from token: " + role);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                System.out.println("🔑 UserDetails authorities: " + userDetails.getAuthorities());

                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("✅ Authentication SUCCESS");
                    System.out.println("🎯 SecurityContext authorities: " +
                            SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                } else {
                    System.out.println("❌ Token INVALID");
                }
            } else {
                System.out.println("ℹ️ Already authenticated or username null");
            }
        } catch (Exception e) {
            System.out.println("💥 Authentication ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== 🛡️ JWT FILTER END ===");
        filterChain.doFilter(request, response);
    }
}