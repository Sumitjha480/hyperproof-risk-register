package com.hyperproof.riskregister.service;

import com.hyperproof.riskregister.api.dto.RiskDetailResponse;
import com.hyperproof.riskregister.api.dto.RiskRequest;
import com.hyperproof.riskregister.api.dto.RiskSummaryResponse;
import com.hyperproof.riskregister.api.mapper.RiskResponseMapper;
import com.hyperproof.riskregister.domain.Risk;
import com.hyperproof.riskregister.domain.RiskCategory;
import com.hyperproof.riskregister.domain.RiskStatus;
import com.hyperproof.riskregister.exception.BusinessRuleViolationException;
import com.hyperproof.riskregister.exception.ResourceNotFoundException;
import com.hyperproof.riskregister.repository.RiskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class RiskService {

    public static final String CLOSE_WITHOUT_MITIGATION_CODE = "RISK_CANNOT_CLOSE_WITHOUT_MITIGATION";

    private final RiskRepository riskRepository;
    private final RiskResponseMapper mapper;

    public RiskService(RiskRepository riskRepository, RiskResponseMapper mapper) {
        this.riskRepository = riskRepository;
        this.mapper = mapper;
    }

    @Transactional
    public RiskDetailResponse create(RiskRequest request) {
        enforceClosable(request.status(), 0);
        Risk risk = new Risk(
                cleanRequired(request.title()),
                cleanOptional(request.description()),
                request.category(),
                cleanRequired(request.owner()),
                request.likelihood(),
                request.impact(),
                request.status(),
                request.nextReviewDate(),
                request.frameworkFunctions()
        );
        return mapper.toDetail(riskRepository.save(risk));
    }

    @Transactional(readOnly = true)
    public List<RiskSummaryResponse> list(
            RiskCategory category,
            RiskStatus status,
            ResidualSort sort
    ) {
        Comparator<RiskSummaryResponse> comparator = Comparator.comparingInt(RiskSummaryResponse::residualScore);
        if (sort == ResidualSort.DESC) {
            comparator = comparator.reversed();
        }
        comparator = comparator.thenComparing(RiskSummaryResponse::title, String.CASE_INSENSITIVE_ORDER);

        return riskRepository.findAllWithMitigations(category, status).stream()
                .map(mapper::toSummary)
                .sorted(comparator)
                .toList();
    }

    @Transactional(readOnly = true)
    public RiskDetailResponse get(UUID id) {
        return mapper.toDetail(findRisk(id));
    }

    @Transactional
    public RiskDetailResponse update(UUID id, RiskRequest request) {
        Risk risk = findRisk(id);
        enforceClosable(request.status(), risk.getMitigations().size());
        risk.update(
                cleanRequired(request.title()),
                cleanOptional(request.description()),
                request.category(),
                cleanRequired(request.owner()),
                request.likelihood(),
                request.impact(),
                request.status(),
                request.nextReviewDate(),
                request.frameworkFunctions()
        );
        return mapper.toDetail(risk);
    }

    @Transactional
    public void delete(UUID id) {
        Risk risk = findRisk(id);
        riskRepository.delete(risk);
    }

    Risk findRisk(UUID id) {
        return riskRepository.findByIdWithMitigations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Risk " + id + " was not found"));
    }

    private void enforceClosable(RiskStatus status, int mitigationCount) {
        if (status == RiskStatus.CLOSED && mitigationCount == 0) {
            throw new BusinessRuleViolationException(
                    CLOSE_WITHOUT_MITIGATION_CODE,
                    "A risk cannot be marked Closed until at least one mitigation is recorded"
            );
        }
    }

    private String cleanRequired(String value) {
        return value.trim();
    }

    private String cleanOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
