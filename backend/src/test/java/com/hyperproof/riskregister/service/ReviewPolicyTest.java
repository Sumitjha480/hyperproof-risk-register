package com.hyperproof.riskregister.service;

import com.hyperproof.riskregister.domain.RiskStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewPolicyTest {

    private final ReviewPolicy policy = new ReviewPolicy(
            Clock.fixed(Instant.parse("2026-08-23T00:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void marksActiveRiskOverdueWhenReviewDateIsBeforeToday() {
        assertThat(policy.isOverdue(LocalDate.of(2026, 8, 22), RiskStatus.OPEN)).isTrue();
        assertThat(policy.isOverdue(LocalDate.of(2026, 8, 23), RiskStatus.OPEN)).isFalse();
    }

    @Test
    void closedRisksDoNotShowAsOverdue() {
        assertThat(policy.isOverdue(LocalDate.of(2026, 8, 22), RiskStatus.CLOSED)).isFalse();
    }

    @Test
    void missingReviewDateIsNotOverdue() {
        assertThat(policy.isOverdue(null, RiskStatus.MITIGATING)).isFalse();
    }
}
