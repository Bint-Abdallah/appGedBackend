package org.example.appgedbackend.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
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

        System.out.println("🔍 JwtFilter - " + method + " " + path);

        // LAISSER PASSER les URLs publiques sans token
        if (path.startsWith("/auth") ||
                path.startsWith("/api/setup") ||
                path.contains("/download") ||  // ← AJOUT CRITIQUE
                path.contains("/preview")) {   // ← AJOUT CRITIQUE

            System.out.println("✅ JwtFilter - Route publique, passage direct: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("❌ JwtFilter - Pas de header Authorization pour: " + path);
                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);

            System.out.println("👤 JwtFilter - Username extrait: " + username + " pour: " + path);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    System.out.println("✅ JwtFilter - Token valide pour: " + path);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("🎭 JwtFilter - Authorities: " + userDetails.getAuthorities());
                } else {
                    System.out.println("❌ JwtFilter - Token invalide pour: " + path);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token expiré", "Votre session a expiré, veuillez vous reconnecter");

        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token invalide", "Signature du token invalide");

        } catch (MalformedJwtException e) {
            logger.warn("Malformed JWT token: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token invalide", "Token malformé");

        } catch (Exception e) {
            logger.error("JWT authentication error: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Erreur d'authentification", "Token invalide");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"error\": \"%s\", \"message\": \"%s\", \"status\": %d}",
                error, message, status
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}