package com.projekat.report_service.model;

import jakarta.persistence.*;
import java.util.List;

//import com.projekat.report_service.model.Media;

@Entity
@Table(name = "prijave")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String address; 

    @Column(name = "user_id")
    private Long userId; 

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<Media> media;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<Upvote> upvotes;

    @ManyToOne
    @JoinColumn(name = "parent_report_id")
    private Report parentReport;
    
    public Report() {
    }

    public Report(String title, String description, String address, Long userId, Status status, Category category) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.userId = userId;
        this.status = status;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<Media> getMedia() { return media; }
    public void setMedia(List<Media> media) { this.media = media; }

    public List<Upvote> getUpvotes() { return upvotes; }
    public void setUpvotes(List<Upvote> upvotes) { this.upvotes = upvotes; }

    public Report getParentReport() { return parentReport; }
    public void setParentReport(Report parentReport) { this.parentReport = parentReport; }
}