package com.example.my_application;

import java.util.List;

public class VisitModel {
    private String visitKey;
    private String date;
    private String time;
    private String diagnosis;
    private String notes;
    private List<String> imageUrls;
    private List<String> thumbnails;

    public VisitModel() {}

    public String getVisitKey() { return visitKey; }
    public void setVisitKey(String visitKey) { this.visitKey = visitKey; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getThumbnails() { return thumbnails; }
    public void setThumbnails(List<String> thumbnails) { this.thumbnails = thumbnails; }
}
