package org.example.appgedbackend.Repository;

import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;
import org.example.appgedbackend.Entity.Document;
import org.example.appgedbackend.Entity.Project;
import org.example.appgedbackend.enums.Category;
import org.example.appgedbackend.enums.DocumentStatus;
import org.example.appgedbackend.enums.Phase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {
    List<Document> findByProject(Project project);
    List<Document> findByCategory(Category category);
    List<Document> findByTitreContainingIgnoreCase(String titre);
    List<Document> findByProjectAndCategory(Project project, Category category);
    long countByProject(Project project);
    List<Document> findAll();
    long countByProjectAndStatus(Project project, DocumentStatus status);
    List<Document> findByProjectId(Long projectId);
    @Query("SELECT COUNT(d) FROM Document d WHERE d.author.username = :username")
    Long countDocumentsCreatedByUser(@Param("username") String username);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.author.username = :username")
    Long countByAuthorUsername(@Param("username") String username);
    @Query("SELECT d FROM Document d WHERE d.dateCreation < :date AND d.archived = false")
    List<Document> findOldDocumentsNotArchived(@Param("date") LocalDate date);

    // ✅ Compter les documents archivés
    long countByArchivedTrue();

    // ✅ Trouver les documents par statut d'archivage
    List<Document> findByArchived(boolean archived);

    long countByArchivedFalse();

    List<Document> findByDateDelivranceBeforeAndArchivedFalse(LocalDate date);

    // Méthodes avec filtrage par archivage
    List<Document> findByArchivedTrue();
    List<Document> findByArchivedFalse();
    List<Document> findByProjectAndCategoryAndArchivedFalse(Project project, Category category);
    long countByProjectAndArchivedFalse(Project project);
    long countByProjectAndStatusAndArchivedFalse(Project project, DocumentStatus status);

    Optional<Document> findByIdAndArchivedFalse(Long id);
    List<Document> findByProjectIdAndArchivedFalse(Long projectId);
    Long countByAuthorUsernameAndArchivedFalse(String username);
    List<Document> findByAuthorUsernameAndArchivedFalse(String username);

    // Méthode alternative avec @Query si les noms sont trop longs
    @Query("SELECT d FROM Document d WHERE d.project.id = :projectId AND d.archived = false")
    List<Document> findActiveDocumentsByProject(@Param("projectId") Long projectId);
    @Query("SELECT d FROM Document d WHERE d.archived = false AND d.dateDelivrance <= :cutoffDate")
    List<Document> findByEligibleForArchive(@Param("cutoffDate") LocalDate cutoffDate);
    List<Document> findByPhaseAndCategoryAndArchivedFalse(Phase phase, Category category);
    List<Document> findByPhaseAndArchivedFalse(Phase phase);
    List<Document> findByProjectAndPhaseAndCategoryAndArchivedFalse(Project project, Phase phase, Category category);
}
