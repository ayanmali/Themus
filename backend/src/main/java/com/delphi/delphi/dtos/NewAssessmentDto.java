package com.delphi.delphi.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.delphi.delphi.utils.AssessmentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewAssessmentDto {
    private String name; // title
    private String description; // description
    private String roleName; // role
    private AssessmentType assessmentType; // type
    private LocalDateTime startDate; // start date
    private LocalDateTime endDate;
    private Integer duration; // estimated duration/time limit
    private List<String> skills;
    private List<String> languageOptions;
}
