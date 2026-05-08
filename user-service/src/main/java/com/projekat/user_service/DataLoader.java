package com.projekat.user_service;

import com.projekat.user_service.model.User;
import com.projekat.user_service.model.UserRole;
import com.projekat.user_service.repository.UserRepository;
import com.projekat.user_service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            UserRole admin = roleRepository.save(new UserRole("ADMIN"));
            UserRole user  = roleRepository.save(new UserRole("USER"));
            UserRole mod   = roleRepository.save(new UserRole("MODERATOR"));

            userRepository.save(User.builder().username("admin").password("admin123")
                    .email("admin@neighboralert.ba").userScore(100).role(admin).build());
            userRepository.save(User.builder().username("marko").password("lozinka123")
                    .email("marko@test.com").userScore(55).role(user).build());
            userRepository.save(User.builder().username("ana").password("lozinka123")
                    .email("ana@test.com").userScore(30).role(user).build());
            userRepository.save(User.builder().username("moderator1").password("mod123")
                    .email("mod@neighboralert.ba").userScore(80).role(mod).build());

            log.info("Inicijalni podaci uspješno učitani.");
        }
    }
}
