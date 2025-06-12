package com.delphi.delphi.dtos;

public class NewUserDto {
    private String name;
    private String email;
    private String organizationName;
    private String password;

    public NewUserDto() {
    }

    public NewUserDto(String name, String email, String organizationName, String password) {
        this.name = name;
        this.email = email;
        this.organizationName = organizationName;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
