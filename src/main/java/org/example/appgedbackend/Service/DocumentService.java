package org.example.appgedbackend.Service;

import jakarta.transaction.Transactional;
import org.example.appgedbackend.Entity.Document;
import org.example.appgedbackend.Entity.Project;
import org.example.appgedbackend.Entity.User;
import org.example.appgedbackend.Repository.DocumentRepository;
import org.example.appgedbackend.Repository.DownloadStatRepository;
import org.example.appgedbackend.Repository.ProjectRepository;
import org.example.appgedbackend.Repository.UserRepository;
import org.example.appgedbackend.dto.DocumentDto;
import org.example.appgedbackend.enums.Category;
import org.example.appgedbackend.enums.DocumentStatus;
import org.example.appgedbackend.enums.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DownloadStatRepository downloadStatRepository;

    // ✅ Convertir Document → DocumentDto
    private DocumentDto toDto(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setTitre(document.getTitre());
        dto.setPhase(document.getPhase());
        dto.setCategory(document.getCategory());
        dto.setSize(document.getSize());
        dto.setDateCreation(document.getDateCreation().toString());
        dto.setDateDelivrance(document.getDateDelivrance() != null ?
                document.getDateDelivrance().toString() : null);

        dto.setFilePath(document.getFilePath());
        dto.setStatus(document.getStatus());
        dto.setArchived(document.isArchived()); // ✅ Ajout du champ archived

        if (document.getAuthor() != null) {
            DocumentDto.AuthorDto authorDto = new DocumentDto.AuthorDto();
            authorDto.setId(document.getAuthor().getId());
            authorDto.setUsername(document.getAuthor().getUsername());
            authorDto.setFullName(document.getAuthor().getFullName());
            dto.setAuthor(authorDto);
        }

        if (document.getProject() != null) {
            DocumentDto.ProjectDto projectDto = new DocumentDto.ProjectDto();
            projectDto.setId(document.getProject().getId());
            projectDto.setNom(document.getProject().getNom());
            projectDto.setDescription(document.getProject().getDescription());
            dto.setProject(projectDto);
        }

        return dto;
    }

    // ✅ Récupérer tous les documents ACTIFS (non archivés)
    public List<DocumentDto> getAllDocumentsDto() {
        return documentRepository.findByArchivedFalse().stream() // ✅ Seulement les non archivés
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Récupérer tous les documents (y compris archivés) - pour l'admin
    public List<DocumentDto> getAllDocumentsIncludingArchivedDto() {
        return documentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Récupérer un document par ID (inclut les archivés)
    public Optional<DocumentDto> getDocumentByIdDto(Long id) {
        return documentRepository.findById(id).map(this::toDto);
    }

    // ✅ Récupérer seulement les documents actifs par ID
    public Optional<DocumentDto> getActiveDocumentByIdDto(Long id) {
        return documentRepository.findByIdAndArchivedFalse(id).map(this::toDto);
    }

    // ✅ Créer un document (par défaut en statut EN_ATTENTE et non archivé)
    public DocumentDto saveDocumentDto(String titre, Phase phase, Category category, String projectName,
                                       MultipartFile file, String username, LocalDate dateDelivrance) throws IOException {
        Project project = projectRepository.findByNom(projectName)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec le nom: " + projectName));

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Document document = new Document();
        document.setTitre(titre);
        document.setPhase(phase); // ✅ Définition de la phase
        document.setCategory(category);
        document.setProject(project);
        document.setAuthor(author);
        document.setDateCreation(LocalDate.now());
        document.setSize(file.getSize());
        document.setStatus(DocumentStatus.EN_ATTENTE);
        document.setDateDelivrance(dateDelivrance);
        document.setArchived(false);

        // Sauvegarde du fichier
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        document.setFilePath(filePath.toString());

        Document saved = documentRepository.save(document);
        return toDto(saved);
    }

    // ✅ Mettre à jour un document (titre, catégorie, projet, statut)
    public DocumentDto updateDocumentDto(Long id, DocumentDto documentDto) {
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        if (documentDto.getTitre() != null) {
            existingDocument.setTitre(documentDto.getTitre());
        }
        if (documentDto.getPhase() != null) { // ✅ Mise à jour de la phase
            existingDocument.setPhase(documentDto.getPhase());
        }
        if (documentDto.getCategory() != null) {
            existingDocument.setCategory(documentDto.getCategory());
        }

        if (documentDto.getProject() != null && documentDto.getProject().getId() != null) {
            Project project = projectRepository.findById(documentDto.getProject().getId())
                    .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
            existingDocument.setProject(project);
        }

        // ✅ Gestion de la date de délivrance
        if (documentDto.getDateDelivrance() != null) {
            try {
                LocalDate dateDelivrance = LocalDate.parse(documentDto.getDateDelivrance());
                existingDocument.setDateDelivrance(dateDelivrance);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Format de date de délivrance invalide");
            }
        } else {
            existingDocument.setDateDelivrance(null);
        }

        Document updatedDocument = documentRepository.save(existingDocument);
        return toDto(updatedDocument);
    }

    // ✅ Supprimer un document
    public void deleteDocument(Long id) {
        downloadStatRepository.deleteByDocumentId(id);
        documentRepository.deleteById(id);
    }

    // ✅ Archiver un document (soft delete)
    public DocumentDto archiveDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        document.setArchived(true);
        Document archived = documentRepository.save(document);
        return toDto(archived);
    }

    // ✅ Restaurer un document archivé
    public DocumentDto restoreDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        document.setArchived(false);
        Document restored = documentRepository.save(document);
        return toDto(restored);
    }

    // ✅ Documents par projet et catégorie (seulement actifs)
    public List<DocumentDto> getDocumentsByProjectAndCategoryDto(Long projectId, Category category) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        return documentRepository.findByProjectAndCategoryAndArchivedFalse(project, category)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Compter tous les documents ACTIFS d'un projet
    public long countDocumentsByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        return documentRepository.countByProjectAndArchivedFalse(project);
    }

    // ✅ Compter seulement les documents validés ACTIFS d'un projet
    public long countValidatedDocumentsByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        return documentRepository.countByProjectAndStatusAndArchivedFalse(project, DocumentStatus.VALIDE);
    }

    // ✅ Récupérer les documents archivés
    public List<DocumentDto> getArchivedDocumentsDto() {
        // 1️⃣ Calculer la date limite pour l'archivage
        LocalDate cutoffDate = LocalDate.now().minusYears(5);

        // 2️⃣ Archiver automatiquement les documents éligibles
        List<Document> eligibleDocs = documentRepository.findByEligibleForArchive(cutoffDate);
        for (Document doc : eligibleDocs) {
            doc.setArchived(true);
            documentRepository.save(doc);
        }

        // 3️⃣ Retourner tous les documents archivés
        return documentRepository.findByArchivedTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Forcer l'archivage des documents dont dateDelivrance > 5 ans
    public void forceArchiveOldDocuments() {
        List<Document> allDocuments = documentRepository.findAll();
        LocalDate now = LocalDate.now();

        for (Document doc : allDocuments) {
            if (doc.getDateDelivrance() != null) {
                LocalDate delivranceDate = doc.getDateDelivrance().atStartOfDay().toLocalDate();
                if (ChronoUnit.YEARS.between(delivranceDate, now) >= 5) {
                    doc.setArchived(true);
                }
            }
        }

        documentRepository.saveAll(allDocuments);
    }

    // ✅ Récupérer les documents actifs
    public List<DocumentDto> getActiveDocumentsDto() {
        return documentRepository.findByArchivedFalse().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Archivage automatique des documents de plus de 5 ans
    // ✅ Archivage automatique des documents dont la date de délivrance ≥ 5 ans
    @Scheduled(cron = "0 0 2 * * ?") // Exécution quotidienne à 2h du matin
    @Transactional
    public void archiveOldDocuments() {
        LocalDate fiveYearsAgo = LocalDate.now().minusYears(5);

        // 👉 On archive les documents dont la date de délivrance est antérieure ou égale à fiveYearsAgo
        List<Document> oldDocuments = documentRepository
                .findByDateDelivranceBeforeAndArchivedFalse(fiveYearsAgo);

        if (!oldDocuments.isEmpty()) {
            for (Document document : oldDocuments) {
                document.setArchived(true);
            }
            documentRepository.saveAll(oldDocuments);

            // Log pour le suivi
            System.out.println("📁 Archivage automatique : " + oldDocuments.size() +
                    " documents archivés (date de délivrance ≤ " + fiveYearsAgo + ")");
        }
    }


    // ✅ Compter les documents archivés
    public long countArchivedDocuments() {
        return documentRepository.countByArchivedTrue();
    }

    // ✅ Compter les documents actifs
    public long countActiveDocuments() {
        return documentRepository.countByArchivedFalse();
    }

    // ✅ Récupérer un Document (utilisé dans AdminController) - inclut les archivés
    public Optional<Document> findById(Long id) {
        return documentRepository.findById(id);
    }

    // ✅ Sauvegarder un Document (utilisé dans AdminController)
    public Document save(Document document) {
        return documentRepository.save(document);
    }

    // ✅ Documents par projet (seulement actifs)
    public List<DocumentDto> getDocumentsByProject(Long projectId) {
        return documentRepository.findByProjectIdAndArchivedFalse(projectId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Compte combien de documents ACTIFS un utilisateur a créés
    public Long countDocumentsCreatedByUser(String username) {
        return documentRepository.countByAuthorUsernameAndArchivedFalse(username);
    }

    // ✅ Documents par utilisateur (seulement actifs)
    public List<DocumentDto> getDocumentsByUser(String username) {
        return documentRepository.findByAuthorUsernameAndArchivedFalse(username)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Ajouter des méthodes de recherche par phase si besoin
    public List<DocumentDto> getDocumentsByPhaseAndCategoryDto(Phase phase, Category category) {
        return documentRepository.findByPhaseAndCategoryAndArchivedFalse(phase, category)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<DocumentDto> getDocumentsByPhaseDto(Phase phase) {
        return documentRepository.findByPhaseAndArchivedFalse(phase)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

}