package org.example.appgedbackend.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private boolean readStatus = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Si tu veux notifier un utilisateur spécifique
    private String username;

    private String type; // "DOCUMENT_ADDED", "DOCUMENT_APPROVED", "DOCUMENT_REJECTED"
    private Long documentId;
    private String actionBy; // Utilisateur qui a fait l'action

    public Notification() {
    }

    public Notification(String message, String username, String type, Long documentId, String actionBy) {
        this.message = message;
        this.username = username;
        this.type = type;
        this.documentId = documentId;
        this.actionBy = actionBy;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
