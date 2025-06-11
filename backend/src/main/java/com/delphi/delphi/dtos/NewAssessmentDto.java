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
    private String name;
    private String description;
    private String roleName;
    private AssessmentType assessmentType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer duration;
    private List<String> skills;
    private List<String> languageOptions;
}
