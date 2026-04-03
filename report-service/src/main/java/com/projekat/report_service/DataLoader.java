package com.projekat.report_service;

import com.projekat.report_service.model.*;
import com.projekat.report_service.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final ReportRepository reportRepository = null;
    private final CategoryRepository categoryRepository = null;
    private final StatusRepository statusRepository = null;

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
        testReport.setCategory(rupa);
        testReport.setStatus(prijavljeno);
        
        reportRepository.save(testReport);

        System.out.println("Podaci su uspješno učitani u bazu!");
    }
}