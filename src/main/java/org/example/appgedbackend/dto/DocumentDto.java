package org.example.appgedbackend.dto;

import org.example.appgedbackend.enums.Category;
import org.example.appgedbackend.enums.DocumentStatus;
import org.example.appgedbackend.enums.Phase;

public class DocumentDto {

    private Long id;
    private String titre;
    private Category category;
    private Phase phase;
    private long size;
    private String dateCreation;  // ISO format
    private String filePath;
    private AuthorDto author;
    private ProjectDto project;
    private DocumentStatus status;
    private String dateDelivrance;
    private boolean archived;
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
    public Phase getPhase() { return phase; }
    public void setPhase(Phase phase) { this.phase = phase; }

    public String getDateDelivrance() {
        return dateDelivrance;
    }

    public void setDateDelivrance(String dateDelivrance) {
        this.dateDelivrance = dateDelivrance;
    }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public AuthorDto getAuthor() { return author; }
    public void setAuthor(AuthorDto author) { this.author = author; }

    public ProjectDto getProject() { return project; }
    public void setProject(ProjectDto project) { this.project = project; }

    // --- DTO interne pour Author ---
    public static class AuthorDto {
        private Long id;
        private String username;
        private String fullName;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }

    // --- DTO interne pour Project ---
    public static class ProjectDto {
        private Long id;
        private String nom;
        private String description;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
