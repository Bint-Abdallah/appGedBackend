package org.example.appgedbackend.Service;


import org.example.appgedbackend.Entity.User;
import org.example.appgedbackend.Repository.UserRepository;
import org.example.appgedbackend.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String fullName, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Nom d'utilisateur déjà pris");
        }

        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.LECTEUR); // rôle par défaut

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
 public void deleteUser (Long userId) {
        userRepository.deleteById(userId);
 }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}

