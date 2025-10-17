package org.example.appgedbackend.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_views")
public class DocumentView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDateTime viewedAt;

    // Constructeurs, getters, setters...

    public DocumentView() {
    }

    public DocumentView(Long id, Long documentId, String username, LocalDateTime viewedAt) {
        this.id = id;
        this.documentId = documentId;
        this.username = username;
        this.viewedAt = viewedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
