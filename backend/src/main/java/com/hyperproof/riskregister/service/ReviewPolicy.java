package com.hyperproof.riskregister.service;

import com.hyperproof.riskregister.domain.RiskStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class ReviewPolicy {
    private final Clock clock;

    public ReviewPolicy(Clock clock) {
        this.clock = clock;
    }

    public boolean isOverdue(LocalDate nextReviewDate, RiskStatus status) {
        return nextReviewDate != null
                && status != RiskStatus.CLOSED
                && nextReviewDate.isBefore(LocalDate.now(clock));
    }
}

