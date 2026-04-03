package com.projekat.user_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "korisnici") // Ovako će se zvati tabela u phpMyAdmin-u
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String rank; 
    private int points;

    // Prazan konstruktor (obavezan za JPA)
    public User() {}

    // Konstruktor za lakši unos podataka kasnije
    public User(String username, String email, String rank, int points) {
        this.username = username;
        this.email = email;
        this.rank = rank;
        this.points = points;
    }

    // Getteri i Setteri (da bi mogli pristupiti podacima)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}