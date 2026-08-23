package com.hyperproof.riskregister.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum RiskCategory {
    OPERATIONAL,
    FINANCIAL,
    COMPLIANCE,
    SECURITY,
    STRATEGIC;

    @JsonCreator
    public static RiskCategory from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("category is required");
        }
        return Arrays.stream(values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported category '" + value + "'. Allowed values: Operational, Financial, Compliance, Security, Strategic"));
    }

    @JsonValue
    public String jsonValue() {
        return name();
    }
}
