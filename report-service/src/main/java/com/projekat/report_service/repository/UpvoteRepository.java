package com.projekat.report_service.repository;

import com.projekat.report_service.model.Upvote;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UpvoteRepository extends JpaRepository<Upvote, Long> {
    @Query("SELECT u FROM Upvote u LEFT JOIN FETCH u.report")
    List<Upvote> findAllWithReport();
}