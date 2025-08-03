package com.delphi.delphi.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecordingRequestDto {
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

    // Default constructor
    public RecordingRequestDto() {}

    // Constructor with all fields
    public RecordingRequestDto(String title, String filename, Long fileSize, Integer duration, 
                              String format, Boolean hasAudio, String thumbnailUrl) {
        this.title = title;
        this.filename = filename;
        this.fileSize = fileSize;
        this.duration = duration;
        this.format = format;
        this.hasAudio = hasAudio;
        this.thumbnailUrl = thumbnailUrl;
    }

    // Getters and Setters
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
} 