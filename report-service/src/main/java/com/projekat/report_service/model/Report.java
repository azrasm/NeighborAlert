package com.projekat.report_service.model;

import jakarta.persistence.*;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "prijave")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Description is mandatory")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Address is mandatory")
    private String address;

    private Double latitude;

    private Double longitude;

    @NotNull(message = "User ID mandatory")
    @Column(name = "user_id")
    private Long userId;

    @NotNull(message = "Category is mandatory")
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @NotNull(message = "Status is mandatory")
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

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

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
