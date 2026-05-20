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
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Oštećenje puta"));
            categoryRepository.save(new Category("Sigurnost"));
            categoryRepository.save(new Category("Komunalne usluge"));
            categoryRepository.save(new Category("Okoliš"));
            categoryRepository.save(new Category("Ostalo"));
        }

        if (statusRepository.count() == 0) {
            statusRepository.save(new Status("Prijavljeno"));
            statusRepository.save(new Status("U toku"));
            statusRepository.save(new Status("Riješeno"));
            statusRepository.save(new Status("Odbijeno"));
        }

        if (reportRepository.count() == 0) {
            Category rupa = categoryRepository.findAll().get(0);
            Status prijavljeno = statusRepository.findAll().get(0);

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
}