package org.example.appgedbackend.Controller;
import org.example.appgedbackend.Entity.User;
import org.example.appgedbackend.Repository.UserRepository;
import org.example.appgedbackend.enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SetupController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/create-admin")
    public String createAdminUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName) {

        // Vérifier si l'utilisateur existe déjà
        if (userRepository.findByUsername(username).isPresent()) {
            return "L'utilisateur existe déjà";
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setFullName(fullName);
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);
        return "Administrateur créé avec succès: " + username;
    }
}
