package com.hyperproof.riskregister.api.dto;

import com.hyperproof.riskregister.domain.RiskCategory;
import com.hyperproof.riskregister.domain.RiskFrameworkFunction;
import com.hyperproof.riskregister.domain.RiskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record RiskRequest(
        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must be 200 characters or fewer")
        String title,

        @Size(max = 5000, message = "description must be 5000 characters or fewer")
        String description,

        @NotNull(message = "category is required")
        RiskCategory category,

        @NotBlank(message = "owner is required")
        @Size(max = 200, message = "owner must be 200 characters or fewer")
        String owner,

        @NotNull(message = "likelihood is required")
        @Min(value = 1, message = "likelihood must be an integer between 1 and 5")
        @Max(value = 5, message = "likelihood must be an integer between 1 and 5")
        Integer likelihood,

        @NotNull(message = "impact is required")
        @Min(value = 1, message = "impact must be an integer between 1 and 5")
        @Max(value = 5, message = "impact must be an integer between 1 and 5")
        Integer impact,

        @NotNull(message = "status is required")
        RiskStatus status,

        LocalDate nextReviewDate,

        @Size(max = 6, message = "at most 6 NIST CSF functions may be selected")
        Set<RiskFrameworkFunction> frameworkFunctions
) {
}

