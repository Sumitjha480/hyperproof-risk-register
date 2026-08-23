package com.hyperproof.riskregister.api;

import com.hyperproof.riskregister.api.dto.RiskDetailResponse;
import com.hyperproof.riskregister.api.dto.RiskRequest;
import com.hyperproof.riskregister.api.dto.RiskSummaryResponse;
import com.hyperproof.riskregister.domain.RiskCategory;
import com.hyperproof.riskregister.domain.RiskStatus;
import com.hyperproof.riskregister.service.ResidualSort;
import com.hyperproof.riskregister.service.RiskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @PostMapping
    public ResponseEntity<RiskDetailResponse> create(@Valid @RequestBody RiskRequest request) {
        RiskDetailResponse created = riskService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<RiskSummaryResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "residualScore,desc") String sort
    ) {
        RiskCategory parsedCategory = category == null || category.isBlank() ? null : RiskCategory.from(category);
        RiskStatus parsedStatus = status == null || status.isBlank() ? null : RiskStatus.from(status);
        return riskService.list(parsedCategory, parsedStatus, ResidualSort.from(sort));
    }

    @GetMapping("/{id}")
    public RiskDetailResponse get(@PathVariable UUID id) {
        return riskService.get(id);
    }

    @PutMapping("/{id}")
    public RiskDetailResponse update(@PathVariable UUID id, @Valid @RequestBody RiskRequest request) {
        return riskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        riskService.delete(id);
    }
}
