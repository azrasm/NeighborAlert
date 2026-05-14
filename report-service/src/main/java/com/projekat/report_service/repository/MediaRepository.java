package com.projekat.report_service.repository;

import com.projekat.report_service.model.Media;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    @Query("SELECT m FROM Media m LEFT JOIN FETCH m.report")
    List<Media> findAllWithReport();
}