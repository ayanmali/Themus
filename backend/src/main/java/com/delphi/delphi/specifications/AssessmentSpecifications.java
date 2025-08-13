package com.delphi.delphi.specifications;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.utils.AssessmentStatus;

@Component
public class AssessmentSpecifications {
    public static Specification<Assessment> belongsToUser(Long userId) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Assessment> hasAssessmentId(Long assessmentId) {
        return (root, _, criteriaBuilder) -> {
            if (assessmentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("id"), assessmentId);
        };
    }

    public static Specification<Assessment> hasAssessmentName(String assessmentName) {
        return (root, _, criteriaBuilder) -> {
            if (assessmentName == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("name"), assessmentName);
        };
    }

    public static Specification<Assessment> hasAssessmentStatus(AssessmentStatus assessmentStatus) {
        return (root, _, criteriaBuilder) -> {
            if (assessmentStatus == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), assessmentStatus);
        };
    }

    public static Specification<Assessment> createdAfter(LocalDateTime createdAfter) {
        return (root, _, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<Assessment> createdBefore(LocalDateTime createdBefore) {
        return (root, _, criteriaBuilder) -> {
            if (createdBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<Assessment> startDateAfter(LocalDateTime startDateAfter) {
        return (root, _, criteriaBuilder) -> {
            if (startDateAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("startDate"), startDateAfter);
        };
    }

    public static Specification<Assessment> startDateBefore(LocalDateTime startDateBefore) {
        return (root, _, criteriaBuilder) -> {
            if (startDateBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get("startDate"), startDateBefore);
        };
    }

    public static Specification<Assessment> endDateAfter(LocalDateTime endDateAfter) {
        return (root, _, criteriaBuilder) -> {
            if (endDateAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("endDate"), endDateAfter);
        };
    }

    public static Specification<Assessment> endDateBefore(LocalDateTime endDateBefore) {
        return (root, _, criteriaBuilder) -> {
            if (endDateBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get("endDate"), endDateBefore);
        };
    }

    public static Specification<Assessment> durationBetween(Integer minDuration, Integer maxDuration) {
        return (root, _, criteriaBuilder) -> {
            if (minDuration == null || maxDuration == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.between(root.get("duration"), minDuration, maxDuration);
        };
    }

    public static Specification<Assessment> hasSkills(List<String> skills) {
        return (root, _, criteriaBuilder) -> {
            if (skills == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.isMember(skills, root.get("skills"));
        };
    }

    public static Specification<Assessment> hasSkill(String skill) {
        return (root, _, criteriaBuilder) -> {
            if (skill == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.isMember(skill, root.get("skills"));
        };
    }

    public static Specification<Assessment> hasLanguageOptions(List<String> languageOptions) {
        return (root, _, criteriaBuilder) -> {
            if (languageOptions == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.isMember(languageOptions, root.get("languageOptions"));
        };
    }

    public static Specification<Assessment> hasMetadataKey(String metadataKey) {
        return (root, _, criteriaBuilder) -> {
            if (metadataKey == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.isMember(metadataKey, root.get("metadata"));
        };
    }
}
