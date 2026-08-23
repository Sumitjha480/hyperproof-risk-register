package com.hyperproof.riskregister.api.dto;

import java.time.Instant;
import java.util.UUID;

public record MitigationResponse(
        UUID id,
        UUID riskId,
        String description,
        int effectiveness,
        Instant createdAt
) {
}
