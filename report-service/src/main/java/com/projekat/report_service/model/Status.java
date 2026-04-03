package com.projekat.report_service.model;

import jakarta.persistence.*;

@Entity
public class Status {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Status() {}

    public Status(String name) {
        this.name = name;
    }

    public String getName(){ return name; }
    public void setName(String name) { this.name = name; }
}