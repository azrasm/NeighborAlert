package com.projekat.user_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security konfiguracija za user-service.
 *
 * Pristup mikroservisu direktno (bez gateway-a):
 *   Mikroservisi su dizajnirani da primaju zahtjeve samo od gateway-a
 *   koji je već validirao JWT. Zbog toga user-service otvara sve
 *   endpointe — gateway je taj koji kontroliše pristup.
 *
 *   U produkciji: mikroservisi bi trebali biti firewall-ovani tako
 *   da su dostupni samo na internoj mreži (ne sa interneta).
 *
 * Spring Security ostaje uključen zbog:
 *   - BCrypt hashiranja lozinki
 *   - Mogućnosti proširenja (npr. service-to-service auth u budućnosti)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF nije potreban za REST API koji koristi JWT
            .csrf(AbstractHttpConfigurer::disable)
            // Bez sesija — JWT je stateless autentifikacija
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Svi endpointi su otvoreni — gateway kontroliše pristup
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    /**
     * BCrypt encoder za hashiranje lozinki.
     * Strength 12 je dobar balans između sigurnosti i performansi.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
