package org.example.appgedbackend.Controller;

import org.example.appgedbackend.Entity.User;
import org.example.appgedbackend.Repository.UserRepository;
import org.example.appgedbackend.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.creation.token}")
    private String adminToken;

    public SetupController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/create-admin")
    public String createAdminUser(@RequestBody AdminRequest request,
                                  @RequestHeader("X-ADMIN-TOKEN") String token) {

        // Vérifier le token secret
        if (!adminToken.equals(token)) {
            return "⚠️ Token invalide, accès refusé";
        }

        // Vérifier si l'utilisateur existe déjà
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "L'utilisateur existe déjà";
        }

        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setFullName(request.getFullName());
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);
        return "Administrateur créé avec succès: " + request.getUsername();
    }

    // DTO pour recevoir le JSON
    static class AdminRequest {
        private String username;
        private String password;
        private String fullName;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }
}
