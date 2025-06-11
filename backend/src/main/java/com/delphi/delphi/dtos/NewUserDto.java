package com.delphi.delphi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserDto {
    private String name;
    private String email;
    private String organizationName;
    private String password;
}
