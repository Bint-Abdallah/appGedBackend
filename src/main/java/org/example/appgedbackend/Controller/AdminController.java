package org.example.appgedbackend.Controller;

import org.example.appgedbackend.Entity.Document;
import org.example.appgedbackend.Entity.User;
import org.example.appgedbackend.Service.DocumentNotificationService;
import org.example.appgedbackend.Service.DocumentService;
import org.example.appgedbackend.Service.UserService;
import org.example.appgedbackend.enums.DocumentStatus;
import org.example.appgedbackend.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final DocumentService documentService;
    private final DocumentNotificationService documentNotificationService;

    public AdminController(UserService userService, DocumentService documentService,
                           DocumentNotificationService documentNotificationService) {
        this.userService = userService;
        this.documentService = documentService;
        this.documentNotificationService = documentNotificationService;
    }

    // ✅ Mise à jour du rôle utilisateur + NOTIFICATION
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails adminUser) {

        String newRole = request.get("role");

        if (!isValidRole(newRole)) {
            return ResponseEntity.badRequest().body("Rôle invalide. Les rôles valides sont: LECTEUR, CONTRIBUTEUR, ADMIN");
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        User user = userOpt.get();
        String oldRole = user.getRole().name();
        user.setRole(Role.valueOf(newRole));
        userService.save(user);

        // Notification pour le changement de rôle
        documentNotificationService.onUserRoleUpdated(user, oldRole, newRole, adminUser.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "Rôle mis à jour avec succès",
                "userId", user.getId(),
                "username", user.getUsername(),
                "newRole", user.getRole()
        ));
    }

    private boolean isValidRole(String role) {
        return role != null &&
                (role.equals("LECTEUR") || role.equals("CONTRIBUTEUR") || role.equals("ADMIN"));
    }

    // ✅ Récupération de tous les utilisateurs
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }
  //supprimer un user
    @DeleteMapping("/{userId}")
    public void  deleteUser(@PathVariable Long userId) {
         userService.deleteUser(userId);
    }
    // ✅ Validation d'un document + NOTIFICATION
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/documents/{id}/valider")
    public ResponseEntity<?> validerDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails adminUser) {

        Optional<Document> docOpt = documentService.findById(id);
        if (docOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document non trouvé");
        }

        Document document = docOpt.get();
        document.setStatus(DocumentStatus.VALIDE);
        documentService.save(document);

        // Notification pour la validation
        documentNotificationService.onDocumentApproved(document, adminUser.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "Document validé avec succès",
                "documentId", document.getId(),
                "status", document.getStatus()
        ));
    }

    // ✅ Rejet d'un document + NOTIFICATION
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/documents/{id}/rejeter")
    public ResponseEntity<?> rejeterDocument(
            @PathVariable Long id,
            @RequestParam(required = false) String raison,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails adminUser) {

        Optional<Document> docOpt = documentService.findById(id);
        if (docOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document non trouvé");
        }

        Document document = docOpt.get();
        document.setStatus(DocumentStatus.REJETE);
        documentService.save(document);

        // Notification pour le rejet
        documentNotificationService.onDocumentRejected(document, adminUser.getUsername(),
                raison != null ? raison : "Raison non spécifiée");

        return ResponseEntity.ok(Map.of(
                "message", "Document rejeté avec succès",
                "documentId", document.getId(),
                "status", document.getStatus(),
                "raison", raison != null ? raison : "Non spécifiée"
        ));
    }
}
