package com.projekat.interaction_service.client;

import com.projekat.interaction_service.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback za {@link UserServiceClient} — aktivira se kad user-service nije dostupan.
 */
@Component
public class UserServiceFallback implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceFallback.class);

    @Override
    public UserDTO getUserById(Long id) {
        log.warn("[FALLBACK] user-service nije dostupan — userId={}", id);
        return null;
    }
}
