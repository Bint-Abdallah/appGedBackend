package org.example.appgedbackend.Controller;

import org.example.appgedbackend.Entity.Notification;
import org.example.appgedbackend.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{username}")
    public List<Notification> getUserNotifications(@PathVariable String username) {
        return notificationService.getUserNotifications(username);
    }

    @GetMapping("/{username}/unread")
    public List<Notification> getUnreadNotifications(@PathVariable String username) {
        return notificationService.getUnreadNotifications(username);
    }

    @GetMapping("/{username}/unread-count")
    public int getUnreadCount(@PathVariable String username) {
        return notificationService.getUnreadNotifications(username).size();
    }

    @PostMapping
    public Notification createNotification(@RequestBody NotificationRequest request) {
        return notificationService.createUserNotification(
                request.getUsername(),
                request.getMessage(),
                request.getType(),
                request.getDocumentId(),
                request.getActionBy()
        );
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @PutMapping("/{username}/read-all")
    public void markAllAsRead(@PathVariable String username) {
        notificationService.markAllAsRead(username);
    }
}

// DTO pour la création de notification
class NotificationRequest {
    private String username;
    private String message;
    private String type;
    private Long documentId;
    private String actionBy;

    // Getters et setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getActionBy() {
        return actionBy;
    }

    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }
}
