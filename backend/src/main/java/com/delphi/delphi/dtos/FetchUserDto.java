package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

import com.delphi.delphi.entities.User;

public class FetchUserDto {
    private Long id;
    private String name;
    private String email;
    private String organizationName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private boolean connectedGithub;

    public FetchUserDto() {
    }

    public FetchUserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.organizationName = user.getOrganizationName();
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();
        this.connectedGithub = user.getGithubUsername() != null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public boolean isConnectedGithub() {
        return connectedGithub;
    }

    public void setConnectedGithub(boolean connectedGithub) {
        this.connectedGithub = connectedGithub;
    }
   
}