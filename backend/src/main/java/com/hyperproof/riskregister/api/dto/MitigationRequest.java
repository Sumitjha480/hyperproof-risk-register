package com.hyperproof.riskregister.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MitigationRequest(
        @NotBlank(message = "description is required")
        @Size(max = 2000, message = "description must be 2000 characters or fewer")
        String description,

        @NotNull(message = "effectiveness is required")
        @Min(value = 1, message = "effectiveness must be an integer between 1 and 5")
        @Max(value = 5, message = "effectiveness must be an integer between 1 and 5")
        Integer effectiveness
) {
}
