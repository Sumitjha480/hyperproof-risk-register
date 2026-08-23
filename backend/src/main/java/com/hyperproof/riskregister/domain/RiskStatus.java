package com.hyperproof.riskregister.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum RiskStatus {
    OPEN,
    MITIGATING,
    CLOSED;

    @JsonCreator
    public static RiskStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        return Arrays.stream(values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported status '" + value + "'. Allowed values: Open, Mitigating, Closed"));
    }

    @JsonValue
    public String jsonValue() {
        return name();
    }
}
