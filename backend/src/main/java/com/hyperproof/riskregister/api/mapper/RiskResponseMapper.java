package com.hyperproof.riskregister.api.mapper;

import com.hyperproof.riskregister.api.dto.MitigationResponse;
import com.hyperproof.riskregister.api.dto.RiskDetailResponse;
import com.hyperproof.riskregister.api.dto.RiskSummaryResponse;
import com.hyperproof.riskregister.domain.Mitigation;
import com.hyperproof.riskregister.domain.Risk;
import com.hyperproof.riskregister.scoring.RiskScore;
import com.hyperproof.riskregister.scoring.RiskScoringService;
import com.hyperproof.riskregister.service.ReviewPolicy;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RiskResponseMapper {

    private final RiskScoringService scoringService;
    private final ReviewPolicy reviewPolicy;

    public RiskResponseMapper(RiskScoringService scoringService, ReviewPolicy reviewPolicy) {
        this.scoringService = scoringService;
        this.reviewPolicy = reviewPolicy;
    }

    public RiskSummaryResponse toSummary(Risk risk) {
        RiskScore score = score(risk);
        return new RiskSummaryResponse(
                risk.getId(),
                risk.getTitle(),
                risk.getCategory(),
                risk.getStatus(),
                score.inherentScore(),
                score.inherentSeverity(),
                score.residualScore(),
                score.residualSeverity(),
                risk.getMitigations().size(),
                risk.getNextReviewDate(),
                reviewPolicy.isOverdue(risk.getNextReviewDate(), risk.getStatus()),
                Set.copyOf(risk.getFrameworkFunctions()),
                risk.getCreatedAt(),
                risk.getUpdatedAt()
        );
    }

    public RiskDetailResponse toDetail(Risk risk) {
        RiskScore score = score(risk);
        return new RiskDetailResponse(
                risk.getId(),
                risk.getTitle(),
                risk.getDescription(),
                risk.getCategory(),
                risk.getOwner(),
                risk.getLikelihood(),
                risk.getImpact(),
                risk.getStatus(),
                score.inherentScore(),
                score.inherentSeverity(),
                score.residualScore(),
                score.residualSeverity(),
                risk.getMitigations().size(),
                risk.getMitigations().stream().map(this::toMitigation).toList(),
                risk.getNextReviewDate(),
                reviewPolicy.isOverdue(risk.getNextReviewDate(), risk.getStatus()),
                Set.copyOf(risk.getFrameworkFunctions()),
                risk.getCreatedAt(),
                risk.getUpdatedAt()
        );
    }

    public MitigationResponse toMitigation(Mitigation mitigation) {
        return new MitigationResponse(
                mitigation.getId(),
                mitigation.getRisk().getId(),
                mitigation.getDescription(),
                mitigation.getEffectiveness(),
                mitigation.getCreatedAt()
        );
    }

    private RiskScore score(Risk risk) {
        return scoringService.calculate(
                risk.getLikelihood(),
                risk.getImpact(),
                risk.getMitigations().stream().map(Mitigation::getEffectiveness).toList()
        );
    }
}
