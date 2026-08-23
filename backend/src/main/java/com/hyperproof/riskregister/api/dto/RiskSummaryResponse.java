package com.hyperproof.riskregister.api.dto;

import com.hyperproof.riskregister.domain.RiskCategory;
import com.hyperproof.riskregister.domain.RiskFrameworkFunction;
import com.hyperproof.riskregister.domain.RiskStatus;
import com.hyperproof.riskregister.domain.SeverityBand;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record RiskSummaryResponse(
        UUID id,
        String title,
        RiskCategory category,
        RiskStatus status,
        int inherentScore,
        SeverityBand inherentSeverity,
        int residualScore,
        SeverityBand residualSeverity,
        int mitigationCount,
        LocalDate nextReviewDate,
        boolean reviewOverdue,
        Set<RiskFrameworkFunction> frameworkFunctions,
        Instant createdAt,
        Instant updatedAt
) {
}
