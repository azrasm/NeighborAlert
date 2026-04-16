package com.projekat.interaction_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projekat.interaction_service.exception.ResourceNotFoundException;
import com.projekat.interaction_service.model.ReportFlag;
import com.projekat.interaction_service.repository.ReportFlagRepository;

@Service
public class ReportFlagService {

    @Autowired
    private ReportFlagRepository flagRepository;

    public ReportFlag createFlag(ReportFlag flag) {
        flag.setReviewed(false); // Osiguravamo da je nova prijava uvijek nepregledana
        return flagRepository.save(flag);
    }

    public List<ReportFlag> getAllFlags() {
        return flagRepository.findAll();
    }

    public List<ReportFlag> getUnreviewedFlags() {
        return flagRepository.findByReviewedFalse();
    }

    public ReportFlag markAsReviewed(Long id, boolean reviewedStatus) {
        ReportFlag flag = flagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flag with ID " + id + " not found."));
        
        flag.setReviewed(reviewedStatus);
        return flagRepository.save(flag);
    }
}