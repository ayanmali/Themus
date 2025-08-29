// package com.delphi.delphi.dtos;

// import java.util.List;
// import java.util.stream.Collectors;

// import com.delphi.delphi.entities.Assessment;

// public class FetchAssessmentWithAttemptsDto extends FetchAssessmentDto {
//     // Employer name of the user who created the assessment
//     private String employerName;
//     private List<FetchCandidateAttemptDto> candidateAttemptDtos;

//     public FetchAssessmentWithAttemptsDto(Assessment assessment) {
//         super(assessment);
//         this.employerName = assessment.getUser().getOrganizationName();
//         this.candidateAttemptDtos = assessment.getCandidateAttempts().stream().map(FetchCandidateAttemptDto::new).collect(Collectors.toList());
//     }

//     // public FetchAssessmentWithAttemptsDto(AssessmentCacheDto assessment) {
//     //     super(assessment);
//     //     this.employerName = assessment.getUserId().toString();
//     //     this.candidateAttemptDtos = assessment.getCandidateAttemptIds().stream().map(FetchCandidateAttemptDto::new).collect(Collectors.toList());
//     // }

//     public List<FetchCandidateAttemptDto> getCandidateAttemptDtos() {
//         return candidateAttemptDtos;
//     }

//     public void setCandidateAttemptDtos(List<FetchCandidateAttemptDto> candidateAttemptDtos) {
//         this.candidateAttemptDtos = candidateAttemptDtos;
//     }

//     public String getEmployerName() {
//         return employerName;
//     }

//     public void setEmployerName(String employerName) {
//         this.employerName = employerName;
//     }
// }
