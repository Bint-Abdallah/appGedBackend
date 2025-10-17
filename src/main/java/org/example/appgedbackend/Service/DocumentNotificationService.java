package org.example.appgedbackend.Service;

import org.example.appgedbackend.Entity.Document;
import org.example.appgedbackend.Entity.User;
import org.example.appgedbackend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentNotificationService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    public void onDocumentAdded(Document document, String addedByUsername) {
        String message = String.format("📄 Nouveau document ajouté: '%s' par %s",
                document.getTitre(), addedByUsername);

        notificationService.notifyAllUsersExceptActor(
                message,
                "DOCUMENT_ADDED",
                document.getId(),
                addedByUsername
        );
    }

    public void onDocumentApproved(Document document, String approvedByUsername) {
        String message = String.format("✅ Document approuvé: '%s' par l'administrateur %s",
                document.getTitre(), approvedByUsername);

        // Notifier tous les utilisateurs sauf l'admin qui a approuvé
        notificationService.notifyAllUsersExceptActor(
                message,
                "DOCUMENT_APPROVED",
                document.getId(),
                approvedByUsername
        );

        // Notification spéciale pour l'uploader du document
        if (!document.getAuthor().getUsername().equals(approvedByUsername)) {
            String uploaderMessage = String.format("✅ Votre document '%s' a été approuvé par l'administrateur",
                    document.getTitre());
            notificationService.createUserNotification(
                    document.getAuthor().getUsername(),
                    uploaderMessage,
                    "DOCUMENT_APPROVED",
                    document.getId(),
                    approvedByUsername
            );
        }
    }

    public void onDocumentRejected(Document document, String rejectedByUsername, String reason) {
        String generalMessage = String.format("❌ Document rejeté: '%s' par l'administrateur %s",
                document.getTitre(), rejectedByUsername);

        String detailedMessage = String.format("❌ Votre document '%s' a été rejeté. Raison: %s",
                document.getTitre(), reason);

        // Notifier l'uploader du document avec la raison détaillée
        notificationService.createUserNotification(
                document.getAuthor().getUsername(),
                detailedMessage,
                "DOCUMENT_REJECTED",
                document.getId(),
                rejectedByUsername
        );

        // Notifier les admins (sauf celui qui a rejeté) avec un message général
        List<String> adminUsernames = userRepository.findAdminUsernames();
        for (String adminUsername : adminUsernames) {
            if (!adminUsername.equals(rejectedByUsername)) {
                notificationService.createUserNotification(
                        adminUsername,
                        generalMessage,
                        "DOCUMENT_REJECTED",
                        document.getId(),
                        rejectedByUsername
                );
            }
        }
    }

    public void onDocumentUpdated(Document document, String updatedByUsername) {
        String message = String.format("✏️ Document modifié: '%s' par %s",
                document.getTitre(), updatedByUsername);

        notificationService.notifyAllUsersExceptActor(
                message,
                "DOCUMENT_UPDATED",
                document.getId(),
                updatedByUsername
        );
    }

    public void onDocumentDeleted(Document document, String deletedByUsername) {
        String message = String.format("🗑️ Document supprimé: '%s' par %s",
                document.getTitre(), deletedByUsername);

        notificationService.notifyAllUsersExceptActor(
                message,
                "DOCUMENT_DELETED",
                document.getId(),
                deletedByUsername
        );
    }

    public void onUserRoleUpdated(User user, String oldRole, String newRole, String updatedByUsername) {
        String message = String.format("👤 Rôle modifié: %s a été changé de %s à %s par %s",
                user.getUsername(), oldRole, newRole, updatedByUsername);

        // Notifier tous les admins sauf celui qui a fait la modification
        List<String> adminUsernames = userRepository.findAdminUsernames();
        for (String adminUsername : adminUsernames) {
            if (!adminUsername.equals(updatedByUsername)) {
                notificationService.createUserNotification(
                        adminUsername,
                        message,
                        "USER_ROLE_UPDATED",
                        user.getId(),
                        updatedByUsername
                );
            }
        }

        // Notifier l'utilisateur concerné
        String userMessage = String.format("👤 Votre rôle a été modifié de %s à %s par l'administrateur",
                oldRole, newRole);
        notificationService.createUserNotification(
                user.getUsername(),
                userMessage,
                "USER_ROLE_UPDATED",
                user.getId(),
                updatedByUsername
        );
    }
}
