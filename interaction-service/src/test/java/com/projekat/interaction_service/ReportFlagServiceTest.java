package com.projekat.interaction_service;

import com.projekat.interaction_service.exception.ResourceNotFoundException;
import com.projekat.interaction_service.model.ReportFlag;
import com.projekat.interaction_service.repository.ReportFlagRepository;
import com.projekat.interaction_service.service.ReportFlagService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportFlagServiceTest {

    @Mock
    private ReportFlagRepository flagRepository;

    @InjectMocks
    private ReportFlagService reportFlagService;

    private ReportFlag testFlag;

    @BeforeEach
    void setUp() {
        testFlag = new ReportFlag();
        testFlag.setId(1L);
        testFlag.setReportId(100L);
        testFlag.setUserId(50L);
        testFlag.setReason("Spam sadržaj");
        testFlag.setReviewed(false);
    }

    // 1. Test za kreiranje prijave
    @Test
    void createFlag_ShouldSetReviewedToFalseAndSave() {
        testFlag.setReviewed(true); 
        when(flagRepository.save(any(ReportFlag.class))).thenReturn(testFlag);

        ReportFlag result = reportFlagService.createFlag(testFlag);

        assertNotNull(result);
        assertFalse(result.getReviewed(), "Nova prijava mora imati reviewed postavljen na false");
        verify(flagRepository, times(1)).save(testFlag);
    }

    // 2. Test za dobavljanje svih prijava
    @Test
    void getAllFlags_ShouldReturnList() {
        when(flagRepository.findAll()).thenReturn(Arrays.asList(testFlag));

        List<ReportFlag> result = reportFlagService.getAllFlags();

        assertEquals(1, result.size());
        verify(flagRepository, times(1)).findAll();
    }

    // 3. Test za nepregledane prijave
    @Test
    void getUnreviewedFlags_ShouldReturnOnlyUnreviewed() {
        when(flagRepository.findByReviewedFalse()).thenReturn(Arrays.asList(testFlag));

        List<ReportFlag> result = reportFlagService.getUnreviewedFlags();

        assertEquals(1, result.size());
        assertFalse(result.get(0).getReviewed());
        verify(flagRepository, times(1)).findByReviewedFalse();
    }

    // 4. Test za oznavavanje kao pregledano
    @Test
    void markAsReviewed_WhenExists_ShouldUpdateStatus() {
        when(flagRepository.findById(1L)).thenReturn(Optional.of(testFlag));
        when(flagRepository.save(any(ReportFlag.class))).thenReturn(testFlag);

        ReportFlag result = reportFlagService.markAsReviewed(1L, true);

        assertTrue(result.getReviewed());
        verify(flagRepository, times(1)).save(testFlag);
    }

    // 5. Test za neuspjesno oznacavanje (ID ne postoji)
    @Test
    void markAsReviewed_WhenNotExists_ShouldThrowException() {
        when(flagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            reportFlagService.markAsReviewed(99L, true);
        });

        verify(flagRepository, never()).save(any(ReportFlag.class));
    }
}