package org.example.appgedbackend.Repository;

import jakarta.transaction.Transactional;
import org.example.appgedbackend.Entity.DownloadStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// DownloadStatRepository.java
@Repository
// DownloadStatRepository.java (Version avec méthodes déclaratives)
public interface DownloadStatRepository extends JpaRepository<DownloadStat, Long> {

    // Méthodes de query automatiques
    Long countByDownloadDateAfter(LocalDateTime date);
    Long countByDownloadDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Méthodes personnalisées
    @Query("SELECT COUNT(ds) FROM DownloadStat ds")
    Long countTotalDownloads();
    @Modifying
    @Transactional
    @Query("DELETE FROM DownloadStat d WHERE d.document.id = :documentId")
    void deleteByDocumentId(@Param("documentId") Long documentId);

    @Query("SELECT COUNT(ds) FROM DownloadStat ds WHERE ds.user.id = :userId")
    Long countDownloadsByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(ds) FROM DownloadStat ds WHERE ds.document.id = :documentId")
    Long countDownloadsByDocument(@Param("documentId") Long documentId);
    @Query(value = "SELECT YEAR(ds.download_date) as year, MONTH(ds.download_date) as month, COUNT(*) as count " +
            "FROM download_stat ds " +
            "WHERE ds.download_date >= :startDate " +
            "GROUP BY YEAR(ds.download_date), MONTH(ds.download_date) " +
            "ORDER BY year DESC, month DESC", nativeQuery = true)

    List<Object[]> getMonthlyStats(@Param("startDate") LocalDateTime startDate);

    List<DownloadStat> findTop10ByOrderByDownloadDateDesc();

    @Query("SELECT COUNT(ds) FROM DownloadStat ds WHERE ds.user.username = :username AND ds.action = 'DOWNLOAD'")
    Long countDownloadsByUser(@Param("username") String username);

    @Query("SELECT COUNT(ds) FROM DownloadStat ds WHERE ds.user.username = :username AND ds.action = 'PREVIEW'")
    Long countPreviewsByUser(@Param("username") String username);
    // Total téléchargements d’un utilisateur
    @Query("SELECT COUNT(ds) FROM DownloadStat ds WHERE ds.user.username = :username")
    Long countDownloadsByUsername(@Param("username") String username);

    // Total prévisualisations d’un utilisateur (si tu as une table PreviewStat, sinon tu peux ignorer)
    @Query("SELECT COUNT(ds) FROM DownloadStat ds WHERE ds.user.username = :username AND ds.userAgent LIKE %:preview%")
    Long countPreviewsByUsername(@Param("username") String username, @Param("preview") String preview);
}

