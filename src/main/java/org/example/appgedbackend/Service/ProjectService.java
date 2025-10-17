package org.example.appgedbackend.Service;

import org.example.appgedbackend.Entity.Project;
import org.example.appgedbackend.Repository.ProjectRepository;
import org.example.appgedbackend.dto.ProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/project-logos/";
    private static final String PUBLIC_PATH = "/uploads/project-logos/";



    // Mapper Project -> ProjectDto
    private ProjectDto mapToDto(Project project) {
        int documentsCount = project.getDocuments().size();
        int validatedDocumentsCount = (int) project.getDocuments().stream()
                .filter(doc -> doc.getStatus() != null && doc.getStatus().name().equals("VALIDE"))
                .count();

        ProjectDto dto = new ProjectDto(
                project.getId(),
                project.getNom(),
                project.getDescription(),
                project.getDateCreation(),
                project.getStartDate(),
                project.getEndDate(),
                documentsCount,
                validatedDocumentsCount,
                project.getDocuments().stream().map(doc -> doc.getId()).toList()
        );

        // Nouveau : gérer le logo
        dto.setLogoPath(project.getLogoPath());
        dto.setHasLogo(project.getLogoPath() != null);

        return dto;
    }

    private Project mapToEntity(ProjectDto dto) {
        Project project = new Project();
        project.setId(dto.getId());
        project.setNom(dto.getNom());
        project.setDescription(dto.getDescription());
        project.setDateCreation(dto.getDateCreation());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setLogoPath(dto.getLogoPath());
        return project;
    }


    public ProjectDto createProjectWithLogo(ProjectDto dto, MultipartFile logoFile) throws IOException {
        Project project = new Project();
        project.setNom(dto.getNom());
        project.setDescription(dto.getDescription());
        project.setDateCreation(LocalDate.now());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());

        Project savedProject = projectRepository.save(project);

        if (logoFile != null && !logoFile.isEmpty()) {
            // Création du dossier si nécessaire
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Générer un nom de fichier unique
            String filename = savedProject.getId() + "_" + System.currentTimeMillis() + "_" +
                    logoFile.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + filename);
            Files.copy(logoFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // ⚠️ IMPORTANT : Utilisez PUBLIC_PATH
            savedProject.setLogoPath(PUBLIC_PATH + filename);
            savedProject = projectRepository.save(savedProject);
        }

        return mapToDto(savedProject);
    }

    // Modifiez aussi la méthode createProject existante pour gérer le cas sans logo
    public ProjectDto createProject(ProjectDto dto) {
        Project project = new Project();
        project.setNom(dto.getNom());
        project.setDescription(dto.getDescription());
        project.setDateCreation(LocalDate.now());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        // Pas de logoPath ici car créé sans logo

        Project saved = projectRepository.save(project);
        return mapToDto(saved);
    }

    public ProjectDto updateProject(Long id, ProjectDto dto) {
        return projectRepository.findById(id).map(project -> {
            project.setNom(dto.getNom());
            project.setDescription(dto.getDescription());
            project.setStartDate(dto.getStartDate());
            project.setEndDate(dto.getEndDate());
            if (dto.getLogoPath() != null) {
                project.setLogoPath(dto.getLogoPath());
            }
            Project updated = projectRepository.save(project);
            return mapToDto(updated);
        }).orElse(null);
    }
    // Mapper ProjectDto -> Project


    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProjectDto> getProjectById(Long id) {
        return projectRepository.findById(id).map(this::mapToDto);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public boolean projectExists(String nom) {
        return projectRepository.existsByNom(nom);
    }

    public List<ProjectDto> searchProjects(String nom) {
        return projectRepository.findByNomContainingIgnoreCase(nom)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    // ✅ Upload logo
    public ProjectDto uploadLogo(Long projectId, MultipartFile file) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Création du dossier si nécessaire
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Générer un nom de fichier unique
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + filename);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        // Sauvegarder le chemin
        project.setLogoPath("/" + UPLOAD_DIR + filename);
        Project saved = projectRepository.save(project);

        return mapToDto(saved);
    }
}
