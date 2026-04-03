package com.projekat.report_service.model;

import jakarta.persistence.*;

@Entity
public class Media {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    public Media() {}

    public String getUrl(){ return url; }
    public void setUrl(String url) { this.url = url; }
}