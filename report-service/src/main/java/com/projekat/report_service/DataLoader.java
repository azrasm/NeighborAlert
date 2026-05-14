package com.projekat.report_service;

import com.projekat.report_service.model.*;
import com.projekat.report_service.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final ReportRepository reportRepository;
    private final CategoryRepository categoryRepository;
    private final StatusRepository statusRepository;

    public DataLoader(ReportRepository reportRepository, 
                      CategoryRepository categoryRepository, 
                      StatusRepository statusRepository) {
        this.reportRepository = reportRepository;
        this.categoryRepository = categoryRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Category rupa = new Category();
        rupa.setName("Oštećenje puta");
        categoryRepository.save(rupa);

        Status prijavljeno = new Status();
        prijavljeno.setName("Prijavljeno");
        statusRepository.save(prijavljeno);

        Report testReport = new Report();
        testReport.setTitle("Velika rupa - Otoka");
        testReport.setDescription("Rupa duboka 20cm na glavnoj raskrsnici.");
        testReport.setAddress("Bulevar Meše Selimovića");
        
        testReport.setUserId(1L); 
        
        testReport.setCategory(rupa);
        testReport.setStatus(prijavljeno);
        
        reportRepository.save(testReport);
    }
}