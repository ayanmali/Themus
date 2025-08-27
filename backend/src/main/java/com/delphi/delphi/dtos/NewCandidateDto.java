package com.delphi.delphi.dtos;

import java.util.Map;

public class NewCandidateDto {
    private String firstName;
    private String lastName;
    private String email;
    private Map<String, String> metadata;

    public NewCandidateDto() {
    }

    public NewCandidateDto(String firstName, String lastName, String email, Map<String, String> metadata) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.metadata = metadata;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

}