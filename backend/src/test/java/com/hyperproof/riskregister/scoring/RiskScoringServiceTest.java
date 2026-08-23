package com.hyperproof.riskregister.scoring;

import com.hyperproof.riskregister.domain.SeverityBand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskScoringServiceTest {

    private final RiskScoringService scoringService = new RiskScoringService();

    @Test
    void calculatesInherentAsLikelihoodTimesImpact() {
        RiskScore score = scoringService.calculate(4, 5, List.of());

        assertThat(score.inherentScore()).isEqualTo(20);
        assertThat(score.inherentSeverity()).isEqualTo(SeverityBand.CRITICAL);
    }

    @Test
    void zeroMitigationsLeavesResidualEqualToInherent() {
        RiskScore score = scoringService.calculate(5, 5, List.of());

        assertThat(score.residualScore()).isEqualTo(25);
        assertThat(score.residualSeverity()).isEqualTo(SeverityBand.CRITICAL);
    }

    @Test
    void highlyEffectiveMitigationMeaningfullyReducesRisk() {
        RiskScore score = scoringService.calculate(5, 4, List.of(5));

        assertThat(score.inherentScore()).isEqualTo(20);
        assertThat(score.residualScore()).isEqualTo(10);
        assertThat(score.residualSeverity()).isEqualTo(SeverityBand.MEDIUM);
    }

    @Test
    void multipleMitigationsCompoundWithDiminishingReturnsAndRoundUp() {
        RiskScore score = scoringService.calculate(5, 4, List.of(3, 4));

        // ceil(20 * 0.70 * 0.60) = ceil(8.4) = 9
        assertThat(score.residualScore()).isEqualTo(9);
    }

    @Test
    void residualNeverFallsBelowOne() {
        RiskScore score = scoringService.calculate(1, 1, List.of(5, 5, 5));

        assertThat(score.residualScore()).isEqualTo(1);
        assertThat(score.residualSeverity()).isEqualTo(SeverityBand.LOW);
    }

    @ParameterizedTest
    @CsvSource({
            "1, LOW",
            "5, LOW",
            "6, MEDIUM",
            "12, MEDIUM",
            "13, HIGH",
            "19, HIGH",
            "20, CRITICAL",
            "25, CRITICAL"
    })
    void mapsEverySeverityBoundary(int score, SeverityBand expected) {
        assertThat(scoringService.severityFor(score)).isEqualTo(expected);
    }

    @Test
    void rejectsValuesOutsideTheOneToFiveScale() {
        assertThatThrownBy(() -> scoringService.calculate(0, 5, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("likelihood");

        assertThatThrownBy(() -> scoringService.calculate(5, 6, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("impact");

        assertThatThrownBy(() -> scoringService.calculate(5, 5, List.of(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("effectiveness");
    }
}
