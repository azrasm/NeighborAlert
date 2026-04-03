package com.projekat.report_service.repository;

import com.projekat.report_service.model.Upvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpvoteRepository extends JpaRepository<Upvote, Long> {
}