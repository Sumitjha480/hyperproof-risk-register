package com.hyperproof.riskregister.service;

import com.hyperproof.riskregister.api.dto.MitigationRequest;
import com.hyperproof.riskregister.api.dto.MitigationResponse;
import com.hyperproof.riskregister.api.mapper.RiskResponseMapper;
import com.hyperproof.riskregister.domain.Mitigation;
import com.hyperproof.riskregister.domain.Risk;
import com.hyperproof.riskregister.domain.RiskStatus;
import com.hyperproof.riskregister.exception.BusinessRuleViolationException;
import com.hyperproof.riskregister.exception.ResourceNotFoundException;
import com.hyperproof.riskregister.repository.RiskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MitigationService {

    public static final String LAST_CLOSED_MITIGATION_CODE = "CANNOT_REMOVE_LAST_MITIGATION_FROM_CLOSED_RISK";

    private final RiskService riskService;
    private final RiskResponseMapper mapper;
    private final RiskRepository riskRepository;

    public MitigationService(
            RiskService riskService,
            RiskResponseMapper mapper,
            RiskRepository riskRepository
    ) {
        this.riskService = riskService;
        this.mapper = mapper;
        this.riskRepository = riskRepository;
    }

    @Transactional
    public MitigationResponse create(UUID riskId, MitigationRequest request) {
        Risk risk = riskService.findRisk(riskId);
        Mitigation mitigation = new Mitigation(request.description().trim(), request.effectiveness());
        risk.addMitigation(mitigation);
        riskRepository.flush();
        return mapper.toMitigation(mitigation);
    }

    @Transactional
    public MitigationResponse update(UUID riskId, UUID mitigationId, MitigationRequest request) {
        Risk risk = riskService.findRisk(riskId);
        Mitigation mitigation = findMitigation(risk, mitigationId);
        mitigation.update(request.description().trim(), request.effectiveness());
        risk.touch();
        return mapper.toMitigation(mitigation);
    }

    @Transactional
    public void delete(UUID riskId, UUID mitigationId) {
        Risk risk = riskService.findRisk(riskId);
        Mitigation mitigation = findMitigation(risk, mitigationId);

        if (risk.getStatus() == RiskStatus.CLOSED && risk.getMitigations().size() == 1) {
            throw new BusinessRuleViolationException(
                    LAST_CLOSED_MITIGATION_CODE,
                    "The final mitigation cannot be removed while the risk is Closed; reopen the risk first"
            );
        }
        risk.removeMitigation(mitigation);
    }

    private Mitigation findMitigation(Risk risk, UUID mitigationId) {
        return risk.getMitigations().stream()
                .filter(candidate -> candidate.getId().equals(mitigationId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mitigation " + mitigationId + " was not found under risk " + risk.getId()));
    }
}
