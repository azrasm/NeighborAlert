package com.projekat.administration_service;

import com.projekat.administration_service.service.AdministrationService;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AdministrationServiceApplicationTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AdministrationService administrationService;

    @Test
    void contextLoads() {
        // Provjera da li se aplikacija uopšte pali
    }

    @Test
    void testHibernateNPlusOne() {
        Session session = entityManager.unwrap(Session.class);
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        administrationService.getAllAssignments();

        long queryCount = stats.getPrepareStatementCount();
        System.out.println("UKUPNO SQL UPITA: " + queryCount);

        assertTrue(queryCount <= 1, "Detektovan N+1 problem!");
    }
}