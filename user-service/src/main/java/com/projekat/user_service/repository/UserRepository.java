package com.projekat.user_service.repository;

import com.projekat.user_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // --- Entity Graph: izbjegava N+1 problem pri učitavanju role-a ---
    @EntityGraph(value = "User.withRole")
    List<User> findAll();

    @EntityGraph(value = "User.withRole")
    Page<User> findAll(Pageable pageable);

    @EntityGraph(value = "User.withRole")
    Optional<User> findById(Long id);

    // --- Custom upiti koji nisu auto-generisani ---

    // Pretraga korisnika po dijelu usernamea (case-insensitive)
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByUsername(@Param("query") String query);

    // Korisnici sa score-om iznad zadanog praga, sortirani opadajuće
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.userScore >= :minScore ORDER BY u.userScore DESC")
    List<User> findTopUsersByMinScore(@Param("minScore") int minScore);

    // Korisnici po roli (po imenu role)
    @EntityGraph(value = "User.withRole")
    @Query("SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    // Statistika: prosječan score po roli
    @Query("SELECT r.name, AVG(u.userScore) FROM User u JOIN u.role r GROUP BY r.name")
    List<Object[]> getAverageScoreByRole();

    // Provjera postoji li username
    boolean existsByUsername(String username);

    // Provjera postoji li email
    boolean existsByEmail(String email);

    // Bulk ažuriranje score-a za sve korisnike određene role
    @Modifying
    @Query("UPDATE User u SET u.userScore = u.userScore + :bonus WHERE u.role.id = :roleId")
    int addBonusScoreByRole(@Param("roleId") Long roleId, @Param("bonus") int bonus);

    // Paginirana pretraga po roli
    @EntityGraph(value = "User.withRole")
    @Query("SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName")
    Page<User> findByRoleNamePaged(@Param("roleName") String roleName, Pageable pageable);
}
