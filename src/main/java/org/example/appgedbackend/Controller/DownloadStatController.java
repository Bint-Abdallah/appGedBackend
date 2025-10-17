package org.example.appgedbackend.Controller;

import org.example.appgedbackend.Service.DownloadStatService;
import org.example.appgedbackend.Service.DocumentService;
import org.example.appgedbackend.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DownloadStatController {

    private final DownloadStatService downloadStatService;
    private final DocumentService documentService;
    private final UserService userService;

    // Constructeur manuel (pas de Lombok)
    public DownloadStatController(DownloadStatService downloadStatService,
                                  DocumentService documentService,
                                  UserService userService) {
        this.downloadStatService = downloadStatService;
        this.documentService = documentService;
        this.userService = userService;
    }

    /**
     * 📊 Statistiques globales de téléchargement
     */
    @GetMapping("/downloads")
    public ResponseEntity<Map<String, Object>> getDownloadStats() {
        try {
            Map<String, Object> stats = downloadStatService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des statistiques globales"));
        }
    }

    /**
     * 📊 Statistiques de téléchargement pour un utilisateur donné (usage admin)
     */
    @GetMapping("/downloads/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDownloadStats(@PathVariable Long userId) {
        try {
            Long userDownloads = downloadStatService.getUserDownloads(userId);
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "totalDownloads", userDownloads
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des statistiques utilisateur"));
        }
    }

    /**
     * 📊 Statistiques personnelles de l’utilisateur connecté
     */
    @GetMapping("/personal")
    public ResponseEntity<Map<String, Object>> getPersonalStats(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Utilisateur non authentifié"));
            }

            String username = userDetails.getUsername();

            Long downloads = downloadStatService.countDownloadsByUser(username);
            Long previews = downloadStatService.countPreviewsByUser(username); // ✅ MAINTENANT FONCTIONNEL
            Long createdDocs = documentService.countDocumentsCreatedByUser(username);

            return ResponseEntity.ok(Map.of(
                    "downloads", downloads,
                    "previews", previews, // ✅ NE SERA PLUS TOUJOURS 0
                    "createdDocs", createdDocs,
                    "lastUpdated", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des statistiques personnelles"));
        }
    }

}
