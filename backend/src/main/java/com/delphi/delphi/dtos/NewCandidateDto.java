package com.delphi.delphi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCandidateDto {
    private String firstName;
    private String lastName;
    private String email;
    private Long userId;
}
