package com.delphi.delphi.dtos;

public class PasswordLoginDto {
    private String email;
    private String password;

    public PasswordLoginDto() {
    }

    public PasswordLoginDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
    
}
