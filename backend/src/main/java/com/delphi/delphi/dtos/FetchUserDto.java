package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

import com.delphi.delphi.entities.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchUserDto {
    private Long id;
    private String name;
    private String email;
    private String organizationName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public FetchUserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.organizationName = user.getOrganizationName();
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();
    }
}
