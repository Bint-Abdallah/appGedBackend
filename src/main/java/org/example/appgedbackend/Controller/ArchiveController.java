package org.example.appgedbackend.Controller;

import org.example.appgedbackend.Service.DocumentService;
import org.example.appgedbackend.dto.DocumentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/archives")
@CrossOrigin(origins = "http://localhost:5173")
public class ArchiveController {

    @Autowired
    private DocumentService documentService;

    // ✅ Récupérer tous les documents archivés
    @GetMapping
    public ResponseEntity<List<DocumentDto>> getArchivedDocuments() {
        List<DocumentDto> archivedDocuments = documentService.getArchivedDocumentsDto();
        return ResponseEntity.ok(archivedDocuments);
    }
    @PostMapping("/force-archive")
    public ResponseEntity<String> forceArchive() {
        documentService.forceArchiveOldDocuments();
        return ResponseEntity.ok("Archivage forcé exécuté !");
    }

    // ✅ Restaurer un document archivé
    @PutMapping("/{id}/restore")
    public ResponseEntity<DocumentDto> restoreDocument(@PathVariable Long id) {
        try {
            DocumentDto restoredDocument = documentService.restoreDocument(id);
            return ResponseEntity.ok(restoredDocument);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Compter les documents archivés
    @GetMapping("/count")
    public ResponseEntity<Long> countArchivedDocuments() {
        long count = documentService.countArchivedDocuments();
        return ResponseEntity.ok(count);
    }
}
