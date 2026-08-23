package com.hyperproof.riskregister.service;

import com.hyperproof.riskregister.exception.BadRequestException;

public enum ResidualSort {
    ASC,
    DESC;

    public static ResidualSort from(String value) {
        String normalized = value == null || value.isBlank() ? "residualScore,desc" : value.trim();
        if (normalized.equalsIgnoreCase("residualScore,asc")) {
            return ASC;
        }
        if (normalized.equalsIgnoreCase("residualScore,desc")) {
            return DESC;
        }
        throw new BadRequestException("sort must be either 'residualScore,asc' or 'residualScore,desc'");
    }
}
