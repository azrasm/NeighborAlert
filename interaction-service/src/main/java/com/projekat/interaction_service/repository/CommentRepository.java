package com.projekat.interaction_service.repository;

import com.projekat.interaction_service.model.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Postojece metode: save, findById, findAll(), deleteById, count
    
    // sam generise upit na osnovu naziva
    List<Comment> findByReportId(Long reportId);

    Page<Comment> findByReportId(Long reportId, Pageable pageable);

    void deleteByReportId(Long reportId);

    @Query("""
        SELECT c FROM Comment c
        WHERE LOWER(c.text) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<Comment> searchByKeyword(@Param("keyword") String keyword);
}
