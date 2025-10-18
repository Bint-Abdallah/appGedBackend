package org.example.appgedbackend.Controller;

import org.example.appgedbackend.Entity.User;
import org.example.appgedbackend.Repository.UserRepository;
import org.example.appgedbackend.Service.JwtService;
import org.example.appgedbackend.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String fullName = request.get("fullName");
            String password = request.get("password");

            User user = userService.registerUser(username, fullName, password);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
                return ResponseEntity.ok(Map.of(
                        "username", user.getUsername(),
                        "role", user.getRole(),
                        "token", token
                ));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides");
    }


        @Autowired
        private UserRepository userRepository;

        // Récupérer l'utilisateur connecté
        @GetMapping("/me")
        public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
            try {
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token manquant ou invalide");
                }

                String token = authHeader.substring(7); // retirer "Bearer "
                String username = jwtService.extractUsername(token); // récupère le username du token

                if (username == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide");
                }

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

                // Créer un DTO pour éviter d'exposer le mot de passe
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("role", user.getRole());

                return ResponseEntity.ok(userInfo);

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé ou token invalide");
            }
        }


    // Mettre à jour le profil utilisateur
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> updates) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token manquant ou invalide");
            }

            String token = authHeader.substring(7); // retirer "Bearer "
            String username = jwtService.extractUsername(token);

            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Mise à jour des champs autorisés
            if (updates.containsKey("fullName")) {
                user.setFullName(updates.get("fullName"));
            }
            if (updates.containsKey("username")) {
                user.setUsername(updates.get("username"));
            }

            User updatedUser = userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profil mis à jour avec succès");
            response.put("user", Map.of(
                    "id", updatedUser.getId(),
                    "username", updatedUser.getUsername(),
                    "fullName", updatedUser.getFullName(),
                    "role", updatedUser.getRole()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Erreur lors de la mise à jour du profil : " + e.getMessage());
        }
    }



}
