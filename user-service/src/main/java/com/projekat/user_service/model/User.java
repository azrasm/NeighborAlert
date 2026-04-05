package com.projekat.user_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "korisnici")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password; // Dodato prema ERD-u
    private String email;
    
    @Column(name = "user_score")
    private int userScore; // Promenjeno ime da odgovara ERD-u

    @ManyToOne
    @JoinColumn(name = "role_id") // Spoljni ključ prema ERD-u
    private UserRole role;

    public User() {}

    public User(String username, String password, String email, int userScore, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.userScore = userScore;
        this.role = role;
    }

    // Getteri i Setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getUserScore() { return userScore; }
    public void setUserScore(int userScore) { this.userScore = userScore; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}