package org.example.appgedbackend.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.appgedbackend.Entity.Document;
import org.example.appgedbackend.Entity.User;
import org.example.appgedbackend.Service.DocumentNotificationService;
import org.example.appgedbackend.Service.DocumentService;
import org.example.appgedbackend.Service.DownloadStatService;
import org.example.appgedbackend.Service.UserService;
import org.example.appgedbackend.dto.DocumentDto;
import org.example.appgedbackend.enums.Category;
import org.example.appgedbackend.enums.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {
    private static final Logger logger = LoggerFactory.getLogger(DownloadStatService.class);


    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentNotificationService documentNotificationService;
    @Autowired
    private UserService userService;
    @Autowired
    private DownloadStatService downloadStatService;

    // Récupérer tous les documents en DTO
    @GetMapping
    public ResponseEntity<List<DocumentDto>> getAllDocuments() {
        List<DocumentDto> documents = documentService.getAllDocumentsDto();
        return ResponseEntity.ok(documents);
    }

    // Récupérer un document par ID en DTO
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocumentById(@PathVariable Long id) {
        Optional<DocumentDto> documentDto = documentService.getDocumentByIdDto(id);
        return documentDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Créer un document avec fichier + NOTIFICATION
    // Modifier le endpoint createDocument pour accepter une date de délivrance optionnelle
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<DocumentDto> createDocument(
            @RequestParam String titre,
            @RequestParam Category category,
            @RequestParam Phase phase,
            @RequestParam String projectName,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDelivrance, // ✅ Nouveau paramètre optionnel
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) throws IOException {

        DocumentDto createdDocument = documentService.saveDocumentDto(
                titre, phase,category,projectName, file, userDetails.getUsername(), dateDelivrance // ✅ Passer la date
        );

        // Notification pour tous les utilisateurs sauf l'acteur
        Document documentEntity = documentService.findById(createdDocument.getId())
                .orElseThrow(() -> new RuntimeException("Document non trouvé après création"));

        documentNotificationService.onDocumentAdded(documentEntity, userDetails.getUsername());

        return ResponseEntity.ok(createdDocument);
    }

    // Mettre à jour un document (juste métadonnées) + NOTIFICATION
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDto> updateDocument(
            @PathVariable Long id,
            @RequestBody DocumentDto documentDto,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        DocumentDto updatedDocument = documentService.updateDocumentDto(id, documentDto);
        if (updatedDocument != null) {
            // Notification pour tous les utilisateurs sauf l'acteur
            Document documentEntity = documentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Document non trouvé"));

            documentNotificationService.onDocumentUpdated(documentEntity, userDetails.getUsername());

            return ResponseEntity.ok(updatedDocument);
        }
        return ResponseEntity.notFound().build();
    }

    // Supprimer un document + NOTIFICATION (optionnel)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        // Récupérer le document avant suppression pour la notification
        Optional<Document> documentOpt = documentService.findById(id);

        documentService.deleteDocument(id);

        // Notification de suppression (optionnel)
        if (documentOpt.isPresent()) {
            documentNotificationService.onDocumentDeleted(documentOpt.get(), userDetails.getUsername());
        }

        return ResponseEntity.noContent().build();
    }

    // Filtrage par projet et catégorie
    @GetMapping("/project/{projectId}/category/{category}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByProjectAndCategory(@PathVariable Long projectId, @PathVariable Category category) {
        List<DocumentDto> documents = documentService.getDocumentsByProjectAndCategoryDto(projectId, category);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/project/{projectId}/count")
    public ResponseEntity<Long> countDocumentsByProject(@PathVariable Long projectId) {
        long count = documentService.countDocumentsByProject(projectId);
        return ResponseEntity.ok(count);
    }

    // DocumentController.java - Modifier l'endpoint de téléchargement
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) throws IOException {

        DocumentDto doc = documentService.getDocumentByIdDto(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        // Récupérer l'utilisateur connecté
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Track du téléchargement
        downloadStatService.trackDownload(
                documentService.findById(id).orElseThrow(),
                user,
                request
        );

        Path path = Paths.get(doc.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Le fichier est introuvable ou illisible : " + doc.getFilePath());
        }

        // Méthode determineContentType intégrée directement
        String contentType;
        try {
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String originalFileName = path.getFileName().toString();
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex);
        }
        String fileName = doc.getTitre().replaceAll("[^a-zA-Z0-9\\-._]", "_") + extension;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewDocument(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        DocumentDto doc = documentService.getDocumentByIdDto(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        // 🔥 TRACKING DE LA CONSULTATION
        try {
            if (userDetails != null) {
                downloadStatService.trackDocumentView(id, userDetails.getUsername());
            }
        } catch (Exception e) {
            logger.warn("Erreur lors du tracking de la consultation du document {}", id, e);
        }

        Path path = Paths.get(doc.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Le fichier est introuvable ou illisible : " + doc.getFilePath());
        }

        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    // ✅ Ajouter des endpoints pour la recherche par phase
    @GetMapping("/phase/{phase}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByPhase(@PathVariable Phase phase) {
        List<DocumentDto> documents = documentService.getDocumentsByPhaseDto(phase);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/phase/{phase}/category/{category}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByPhaseAndCategory(
            @PathVariable Phase phase, @PathVariable Category category) {
        List<DocumentDto> documents = documentService.getDocumentsByPhaseAndCategoryDto(phase, category);
        return ResponseEntity.ok(documents);
    }
}
