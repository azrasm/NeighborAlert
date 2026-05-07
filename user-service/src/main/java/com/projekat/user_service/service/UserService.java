package com.projekat.user_service.service;

import com.projekat.user_service.dto.UserCreateDTO;
import com.projekat.user_service.model.User;
import com.projekat.user_service.model.UserRole;
import com.projekat.user_service.repository.UserRepository;
import com.projekat.user_service.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;

    public UserService(UserRepository userRepository, UserRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // Koristimo custom metodu iz repozitorijuma da izbjegnemo N+1 problem
    public List<User> getAllUsers() {
        return userRepository.findAllWithRoles(); 
    }

    @Transactional
    public User createUser(UserCreateDTO dto) {
        UserRole role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role sa ID-jem " + dto.getRoleId() + " ne postoji."));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        user.setUserScore(dto.getUserScore());
        user.setRole(role);

        return userRepository.save(user);
    }
}