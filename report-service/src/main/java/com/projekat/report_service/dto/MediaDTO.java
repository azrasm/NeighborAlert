package com.projekat.report_service.dto;

public class MediaDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String url; //URL preko kojeg će se slika moći vidjeti
    private Long reportId; //ID prijave kojoj ova slika pripada

    public MediaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
}