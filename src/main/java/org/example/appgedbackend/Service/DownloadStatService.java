package org.example.appgedbackend.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.example.appgedbackend.Entity.Document;
import org.example.appgedbackend.Entity.DocumentView;
import org.example.appgedbackend.Entity.DownloadStat;
import org.example.appgedbackend.Entity.User;
import org.example.appgedbackend.Repository.DocumentViewRepository;
import org.example.appgedbackend.Repository.DownloadStatRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class DownloadStatService {
    private static final Logger logger = LoggerFactory.getLogger(DownloadStatService.class);
    private final DownloadStatRepository downloadStatRepository;
    private final DocumentViewRepository documentViewRepository;

    public DownloadStatService(DownloadStatRepository downloadStatRepository, DocumentViewRepository documentViewRepository) {
        this.downloadStatRepository = downloadStatRepository;
        this.documentViewRepository = documentViewRepository;
    }

    /**
     * Enregistre une statistique lors du téléchargement d’un document.
     */
    public void trackDownload(Document document, User user, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        DownloadStat downloadStat = new DownloadStat(document, user, ipAddress, userAgent);
        downloadStat.setAction(DownloadStat.Action.DOWNLOAD); // ✅ ajouter l’action

        downloadStatRepository.save(downloadStat);
    }


    /**
     * Nombre total de téléchargements.
     */
    public Long getTotalDownloads() {
        return downloadStatRepository.count();
    }

    /**
     * Nombre total de téléchargements par utilisateur.
     */
    public Long getUserDownloads(Long userId) {
        return downloadStatRepository.countDownloadsByUser(userId);
    }

    /**
     * Statistiques globales pour le tableau de bord.
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total des téléchargements
        stats.put("totalDownloads", downloadStatRepository.count());

        // Téléchargements des 30 derniers jours
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        Long last30DaysDownloads = downloadStatRepository.countByDownloadDateAfter(last30Days);
        stats.put("last30DaysDownloads", last30DaysDownloads);

        // Comparaison avec la période précédente (30-60 jours)
        LocalDateTime last60Days = LocalDateTime.now().minusDays(60);
        LocalDateTime from = last60Days;
        LocalDateTime to = last30Days;

        Long previousPeriodDownloads = downloadStatRepository.countByDownloadDateBetween(from, to);

        double growth = previousPeriodDownloads > 0
                ? ((last30DaysDownloads - previousPeriodDownloads) * 100.0 / previousPeriodDownloads)
                : 0;

        stats.put("growthRate", Math.round(growth * 100.0) / 100.0);
        stats.put("growthTrend", growth >= 0 ? "up" : "down");

        return stats;
    }

    /**
     * Récupère l’adresse IP du client.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-FORWARDED-FOR");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
    // 🔹 Compte les téléchargements d’un utilisateur via username
    public Long countDownloadsByUser(String username) {
        return downloadStatRepository.countDownloadsByUsername(username);
    }



    public void trackDocumentView(Long documentId, String username) {
        try {
            DocumentView view = new DocumentView();
            view.setDocumentId(documentId);
            view.setUsername(username);
            view.setViewedAt(LocalDateTime.now());
            documentViewRepository.save(view);

            logger.info("Consultation trackée - Document: {}, Utilisateur: {}", documentId, username);
        } catch (Exception e) {
            logger.error("Erreur tracking view document {}", documentId, e);
        }
    }
    public Long countPreviewsByUser(String username) {
        return documentViewRepository.countByUsername(username);
    }

    public Long countPreviewsByDocument(Long documentId) {
        return documentViewRepository.countByDocumentId(documentId);
    }

    public Map<String, Object> getUserViewStats(String username) {
        Long totalViews = countPreviewsByUser(username);
        Long last30DaysViews = documentViewRepository.countByUsernameAndViewedAtAfter(
                username, LocalDateTime.now().minusDays(30));

        return Map.of(
                "totalViews", totalViews,
                "last30DaysViews", last30DaysViews,
                "viewsTrend", calculateViewsTrend(username)
        );
    }
    public String calculateViewsTrend(String username) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime last30Days = now.minusDays(30);
            LocalDateTime previous30Days = now.minusDays(60);

            Long currentPeriodViews = documentViewRepository.countByUsernameAndViewedAtAfter(username, last30Days);
            Long previousPeriodViews = documentViewRepository.countByUsernameAndViewedAtAfter(username, previous30Days);

            if (previousPeriodViews == 0) {
                return currentPeriodViews > 0 ? "up" : "stable";
            }

            double growth = ((double) (currentPeriodViews - previousPeriodViews) / previousPeriodViews) * 100;

            if (growth > 10) return "up";
            if (growth < -10) return "down";
            return "stable";

        } catch (Exception e) {
            logger.error("Erreur calcul trend views pour {}", username, e);
            return "stable";
        }
    }
}
