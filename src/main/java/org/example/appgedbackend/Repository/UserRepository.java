package org.example.appgedbackend.Repository;

import org.example.appgedbackend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    // Récupérer tous les noms d'utilisateurs
    @Query("SELECT u.username FROM User u")
    List<String> findAllUsernames();

    // Récupérer les noms d'utilisateurs admin
    @Query("SELECT u.username FROM User u WHERE u.role = 'ADMIN'")
    List<String> findAdminUsernames();
}