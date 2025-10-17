package org.example.appgedbackend.Repository;

import org.example.appgedbackend.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);
    List<Notification> findByUsernameAndReadStatusFalseOrderByCreatedAtDesc(String username);
    void deleteByCreatedAtBefore(LocalDateTime date); // Nettoyage des vieilles notifications
}
