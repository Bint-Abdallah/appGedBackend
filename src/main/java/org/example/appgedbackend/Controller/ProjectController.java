package org.example.appgedbackend.Controller;

import org.example.appgedbackend.Service.DocumentService;
import org.example.appgedbackend.Service.ProjectService;
import org.example.appgedbackend.dto.DocumentDto;
import org.example.appgedbackend.dto.ProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173") // Autoriser les requêtes depuis React
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private DocumentService documentService;

    @GetMapping
    public List<ProjectDto> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
        Optional<ProjectDto> project = projectService.getProjectById(id);
        return project.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    // ✅ Endpoint pour créer un projet avec logo - CORRIGÉ
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectDto> createProject(
            @RequestParam("nom") String nom,
            @RequestParam("description") String description,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {

        try {
            ProjectDto projectDto = new ProjectDto();
            projectDto.setNom(nom);
            projectDto.setDescription(description);
            projectDto.setStartDate(startDate);
            projectDto.setEndDate(endDate);

            ProjectDto createdProject;

            if (logo != null && !logo.isEmpty()) {
                // Créer le projet avec logo
                createdProject = projectService.createProjectWithLogo(projectDto, logo);
            } else {
                // Créer le projet sans logo
                createdProject = projectService.createProject(projectDto);
            }

            return ResponseEntity.ok(createdProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id, @RequestBody ProjectDto dto) {
        ProjectDto updatedProject = projectService.updateProject(id, dto);
        if (updatedProject != null) {
            return ResponseEntity.ok(updatedProject);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<ProjectDto> searchProjects(@RequestParam String nom) {
        return projectService.searchProjects(nom);
    }

    @GetMapping("/exists")
    public boolean projectExists(@RequestParam String nom) {
        return projectService.projectExists(nom);
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<DocumentDto>> getDocumentsByProject(@PathVariable Long id) {
        List<DocumentDto> docs = documentService.getDocumentsByProject(id);
        return ResponseEntity.ok(docs);
    }

    @PostMapping("/{id}/logo")
    public ResponseEntity<ProjectDto> uploadLogo(@PathVariable Long id,
                                                 @RequestParam("file") MultipartFile file) {
        try {
            ProjectDto dto = projectService.uploadLogo(id, file);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
