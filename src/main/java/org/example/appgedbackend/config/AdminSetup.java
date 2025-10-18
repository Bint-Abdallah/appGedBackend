package org.example.appgedbackend.config;


import org.example.appgedbackend.Entity.User;
import org.example.appgedbackend.Repository.UserRepository;
import org.example.appgedbackend.enums.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSetup {

    @Bean
    CommandLineRunner createAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@ged.com";

            if (userRepository.findByUsername(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminEmail);
                admin.setFullName("Admin Super");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
                System.out.println("✅ Admin créé automatiquement !");
            } else {
                System.out.println("ℹ️ Admin déjà existant, création ignorée.");
            }
        };
    }
}
