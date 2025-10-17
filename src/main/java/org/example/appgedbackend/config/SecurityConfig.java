package org.example.appgedbackend.config;

import org.example.appgedbackend.Service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final org.example.appgedbackend.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("https://appgedbackend.onrender.com/login").permitAll()
                        .requestMatchers("https://appgedbackend.onrender.com/register").permitAll()
                        .requestMatchers("/", "/index.html", "/vite.svg", "/assets/**", "/uploads/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/setup/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/archives/**").hasAnyRole("LECTEUR","CONTRIBUTEUR","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/projects/**").hasAnyRole("LECTEUR", "CONTRIBUTEUR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/documents").hasAnyRole("LECTEUR", "CONTRIBUTEUR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/documents/**").hasAnyRole("LECTEUR", "CONTRIBUTEUR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/documents/**").hasAnyRole("CONTRIBUTEUR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/documents/**").hasAnyRole("CONTRIBUTEUR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/documents/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/notifications/**").hasAnyRole("LECTEUR", "CONTRIBUTEUR", "ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}