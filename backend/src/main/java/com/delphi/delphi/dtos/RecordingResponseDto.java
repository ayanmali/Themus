package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecordingResponseDto {
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("filename")
    private String filename;
    
    @JsonProperty("fileSize")
    private Long fileSize;
    
    @JsonProperty("duration")
    private Integer duration;
    
    @JsonProperty("format")
    private String format;
    
    @JsonProperty("hasAudio")
    private Boolean hasAudio;
    
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    // Default constructor
    public RecordingResponseDto() {}

    // Constructor with all fields
    public RecordingResponseDto(Integer id, String title, String filename, Long fileSize, 
                               Integer duration, String format, Boolean hasAudio, 
                               String thumbnailUrl, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.filename = filename;
        this.fileSize = fileSize;
        this.duration = duration;
        this.format = format;
        this.hasAudio = hasAudio;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Boolean getHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(Boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 