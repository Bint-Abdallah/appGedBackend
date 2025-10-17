package org.example.appgedbackend.dto;

import java.time.LocalDate;
import java.util.List;

public class ProjectDto {

    private Long id;
    private String nom;
    private String description;
    private LocalDate dateCreation;
    private LocalDate startDate;
    private LocalDate endDate;

    private int documentsCount;
    private int validatedDocumentsCount;
    // ✅ Nouveau champ pour le logo
    private String logoPath;
    private boolean hasLogo;

    private List<Long> documents; // Liste des IDs des documents liés

    // --- Constructeurs ---
    public ProjectDto() {}

    public ProjectDto(Long id, String nom, String description, LocalDate dateCreation,
                      LocalDate startDate, LocalDate endDate, int documentsCount,
                      int validatedDocumentsCount, List<Long> documentIds) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.dateCreation = dateCreation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.documentsCount = documentsCount;
        this.validatedDocumentsCount = validatedDocumentsCount;
        this.documents = documentIds;
    }

    // --- Getters & Setters ---

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public boolean isHasLogo() {
        return hasLogo;
    }

    public void setHasLogo(boolean hasLogo) {
        this.hasLogo = hasLogo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getDocumentsCount() {
        return documentsCount;
    }

    public void setDocumentsCount(int documentsCount) {
        this.documentsCount = documentsCount;
    }

    public int getValidatedDocumentsCount() {
        return validatedDocumentsCount;
    }

    public void setValidatedDocumentsCount(int validatedDocumentsCount) {
        this.validatedDocumentsCount = validatedDocumentsCount;
    }

    public List<Long> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Long> documents) {
        this.documents = documents;
    }
}
