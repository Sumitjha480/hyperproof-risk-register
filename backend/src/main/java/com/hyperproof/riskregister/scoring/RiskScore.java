package com.hyperproof.riskregister.scoring;

import com.hyperproof.riskregister.domain.SeverityBand;

public record RiskScore(
        int inherentScore,
        SeverityBand inherentSeverity,
        int residualScore,
        SeverityBand residualSeverity
) {
}
