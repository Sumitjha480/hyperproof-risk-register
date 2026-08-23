package com.hyperproof.riskregister.api.dto;

import com.hyperproof.riskregister.domain.RiskCategory;
import com.hyperproof.riskregister.domain.RiskStatus;
import com.hyperproof.riskregister.domain.SeverityBand;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RiskDetailResponse(
        UUID id,
        String title,
        String description,
        RiskCategory category,
        String owner,
        int likelihood,
        int impact,
        RiskStatus status,
        int inherentScore,
        SeverityBand inherentSeverity,
        int residualScore,
        SeverityBand residualSeverity,
        int mitigationCount,
        List<MitigationResponse> mitigations,
        Instant createdAt,
        Instant updatedAt
) {
}
