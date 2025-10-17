package org.example.appgedbackend.Repository;

import org.example.appgedbackend.Entity.DocumentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentViewRepository extends JpaRepository<DocumentView, Long> {
    Long countByUsername(String username);
    Long countByDocumentId(Long documentId);
    Long countByUsernameAndViewedAtAfter(String username, LocalDateTime date);

    @Query("SELECT dv.documentId, COUNT(dv) as viewCount " +
            "FROM DocumentView dv " +
            "WHERE dv.viewedAt >= :startDate " +
            "GROUP BY dv.documentId " +
            "ORDER BY viewCount DESC")
    List<Object[]> findMostViewedDocuments(@Param("startDate") LocalDateTime startDate, Pageable pageable);
}