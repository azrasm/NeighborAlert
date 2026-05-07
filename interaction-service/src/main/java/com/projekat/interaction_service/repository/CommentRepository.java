package com.projekat.interaction_service.repository;

import com.projekat.interaction_service.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Postojece metode: save, findById, findAll(), deleteById, count
    
    // sam generise upit na osnovu naziva
    List<Comment> findByReportId(Long reportId);
}
