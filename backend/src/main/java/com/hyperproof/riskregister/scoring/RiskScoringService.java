package com.hyperproof.riskregister.scoring;

import com.hyperproof.riskregister.domain.SeverityBand;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

@Component
public class RiskScoringService {

    private static final BigDecimal REDUCTION_PER_EFFECTIVENESS_POINT = new BigDecimal("0.10");

    public RiskScore calculate(int likelihood, int impact, Collection<Integer> effectivenessValues) {
        validateScale("likelihood", likelihood);
        validateScale("impact", impact);

        Collection<Integer> values = effectivenessValues == null ? List.of() : effectivenessValues;
        values.forEach(value -> validateScale("effectiveness", value));

        int inherent = likelihood * impact;
        BigDecimal remainingFactor = BigDecimal.ONE;

        for (int effectiveness : values) {
            BigDecimal reduction = REDUCTION_PER_EFFECTIVENESS_POINT.multiply(BigDecimal.valueOf(effectiveness));
            remainingFactor = remainingFactor.multiply(BigDecimal.ONE.subtract(reduction));
        }

        int residual = BigDecimal.valueOf(inherent)
                .multiply(remainingFactor)
                .setScale(0, RoundingMode.CEILING)
                .intValueExact();
        residual = Math.max(1, residual);

        return new RiskScore(inherent, severityFor(inherent), residual, severityFor(residual));
    }

    public SeverityBand severityFor(int score) {
        if (score < 1 || score > 25) {
            throw new IllegalArgumentException("score must be between 1 and 25");
        }
        if (score <= 5) {
            return SeverityBand.LOW;
        }
        if (score <= 12) {
            return SeverityBand.MEDIUM;
        }
        if (score <= 19) {
            return SeverityBand.HIGH;
        }
        return SeverityBand.CRITICAL;
    }

    private void validateScale(String field, int value) {
        if (value < 1 || value > 5) {
            throw new IllegalArgumentException(field + " must be an integer between 1 and 5");
        }
    }
}
