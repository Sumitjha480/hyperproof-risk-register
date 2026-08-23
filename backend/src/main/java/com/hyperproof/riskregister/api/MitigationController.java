package com.hyperproof.riskregister.api;

import com.hyperproof.riskregister.api.dto.MitigationRequest;
import com.hyperproof.riskregister.api.dto.MitigationResponse;
import com.hyperproof.riskregister.service.MitigationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/risks/{riskId}/mitigations")
public class MitigationController {

    private final MitigationService mitigationService;

    public MitigationController(MitigationService mitigationService) {
        this.mitigationService = mitigationService;
    }

    @PostMapping
    public ResponseEntity<MitigationResponse> create(
            @PathVariable UUID riskId,
            @Valid @RequestBody MitigationRequest request
    ) {
        MitigationResponse created = mitigationService.create(riskId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{mitigationId}")
    public MitigationResponse update(
            @PathVariable UUID riskId,
            @PathVariable UUID mitigationId,
            @Valid @RequestBody MitigationRequest request
    ) {
        return mitigationService.update(riskId, mitigationId, request);
    }

    @DeleteMapping("/{mitigationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID riskId, @PathVariable UUID mitigationId) {
        mitigationService.delete(riskId, mitigationId);
    }
}
