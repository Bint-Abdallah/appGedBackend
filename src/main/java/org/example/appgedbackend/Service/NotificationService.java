package org.example.appgedbackend.Service;

import jakarta.transaction.Transactional;
import org.example.appgedbackend.Entity.Notification;
import org.example.appgedbackend.Repository.NotificationRepository;
import org.example.appgedbackend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // Méthode manquante : Récupérer toutes les notifications d'un utilisateur
    public List<Notification> getUserNotifications(String username) {
        return notificationRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    // Méthode existante (déjà dans votre code)
    public List<Notification> getUnreadNotifications(String username) {
        return notificationRepository.findByUsernameAndReadStatusFalseOrderByCreatedAtDesc(username);
    }

    // Méthode manquante : Créer une notification
    public Notification createUserNotification(String username, String message, String type, Long documentId, String actionBy) {
        Notification notification = new Notification();
        notification.setUsername(username);
        notification.setMessage(message);
        notification.setType(type);
        notification.setDocumentId(documentId);
        notification.setActionBy(actionBy);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);

        return notificationRepository.save(notification);
    }

    // Méthode manquante : Notifier tous les utilisateurs sauf l'acteur
    public void notifyAllUsersExceptActor(String message, String type, Long documentId, String actionByUsername) {
        List<String> allUsernames = userRepository.findAllUsernames();

        for (String username : allUsernames) {
            if (!username.equals(actionByUsername)) {
                createUserNotification(username, message, type, documentId, actionByUsername);
            }
        }
    }

    // Méthode existante (déjà dans votre code)
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    // Méthode manquante : Marquer toutes comme lues
    public void markAllAsRead(String username) {
        List<Notification> unreadNotifications = getUnreadNotifications(username);
        for (Notification notification : unreadNotifications) {
            notification.setReadStatus(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
}