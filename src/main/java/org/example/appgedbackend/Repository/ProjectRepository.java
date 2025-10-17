package org.example.appgedbackend.Repository;

import jakarta.persistence.Entity;
import org.example.appgedbackend.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {
    List<Project> findByNomContainingIgnoreCase(String nom);
    boolean existsByNom(String nom);

    Optional<Project> findByNom(String nom);

    Optional<Project> findByNomIgnoreCase(String nom);
}
