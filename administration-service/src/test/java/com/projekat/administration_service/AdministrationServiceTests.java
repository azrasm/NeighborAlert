package com.projekat.administration_service;

import com.projekat.administration_service.dto.ReportAssignmentDTO;
import com.projekat.administration_service.model.ReportAssignment;
import com.projekat.administration_service.repository.ReportAssignmentRepository;
import com.projekat.administration_service.service.AdministrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministrationServiceTests {

    @Mock
    private ReportAssignmentRepository repository;

    @InjectMocks
    private AdministrationService service;

    @Test
    void testAssignReportLogic() {
        // Priprema podataka
        ReportAssignmentDTO dto = new ReportAssignmentDTO();
        dto.setReportId(1L);
        dto.setService("Vodovod");
        dto.setAdminId(10L);

        // Simulacija ponašanja repozitorija
        when(repository.save(any(ReportAssignment.class))).thenReturn(new ReportAssignment());

        // Poziv metode
        service.assignReport(dto);

        // Provjera: Da li je repository.save() pozvan tačno jednom?
        verify(repository, times(1)).save(any(ReportAssignment.class));
    }
}