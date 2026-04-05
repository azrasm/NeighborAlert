package com.projekat.user_service;

import com.projekat.user_service.model.User;
import com.projekat.user_service.model.UserRole;
import com.projekat.user_service.repository.UserRepository;
import com.projekat.user_service.repository.UserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;

    public DataLoader(UserRepository userRepository, UserRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Prvo kreiramo uloge
        UserRole adminRole = roleRepository.save(new UserRole("ADMIN"));
        UserRole userRole = roleRepository.save(new UserRole("USER"));

        // 2. Dodajemo korisnike sa lozinkama i ulogama
        userRepository.save(new User("ajsa", "password123", "ajsa@test.com", 10, userRole));
        userRepository.save(new User("azra", "azra123", "azra@test.com", 90, adminRole));
        userRepository.save(new User("amina", "amina123", "amina@test.com", 50, userRole));
        userRepository.save(new User("lejla", "lejla123", "lejla@test.com", 70, userRole));
        
        System.out.println(">>> User Service: Podaci su uspesno učitani!");
    }
}