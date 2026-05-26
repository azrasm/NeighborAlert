package com.projekat.report_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Status {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Status name is mandatory")
    private String name;

    public Status() {}

    public Status(String name) {
        this.name = name;
    }

    public String getName(){ return name; }
    public void setName(String name) { this.name = name; }

    public Long getId() { return id; }
}