
package com.projekat.user_service;

import com.projekat.user_service.model.User;
import com.projekat.user_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Dodajemo dva korisnika u tabelu
        userRepository.save(new User("ajsa", "ajsa@test.com", "Osnovni nivo", 10));
        userRepository.save(new User("azra", "azra@test.com", "Napredni nivo", 90));
        userRepository.save(new User("amina", "amina@test.com", "Srednji nivo", 90));
        userRepository.save(new User("lejla", "lejla@test.com", "Osnovni nivo", 90));
        
        System.out.println(">>> User Service: Testni korisnici su uneseni u MySQL!");
    }
}