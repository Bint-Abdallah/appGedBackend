package org.example.appgedbackend.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_stats")
public class DownloadStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "download_date", nullable = false)
    private LocalDateTime downloadDate = LocalDateTime.now();

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private Action action;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ─── Enum pour différencier les actions ───────────────
    public enum Action {
        DOWNLOAD,
        PREVIEW
    }

    // ─── Constructeurs ───────────────
    public DownloadStat() {}

    public DownloadStat(Document document, User user, String ipAddress, String userAgent) {
        this.document = document;
        this.user = user;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.action = Action.DOWNLOAD; // ✅ valeur par défaut
    }


    // ─── Getters et Setters ───────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getDownloadDate() { return downloadDate; }
    public void setDownloadDate(LocalDateTime downloadDate) { this.downloadDate = downloadDate; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
}
