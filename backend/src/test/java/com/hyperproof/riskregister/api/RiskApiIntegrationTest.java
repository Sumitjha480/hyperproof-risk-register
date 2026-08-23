package com.hyperproof.riskregister.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperproof.riskregister.repository.RiskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RiskApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RiskRepository riskRepository;

    @BeforeEach
    void cleanDatabase() {
        riskRepository.deleteAll();
    }

    @Test
    void createRiskAddMitigationThenFetchUpdatedResidualScore() throws Exception {
        String riskId = createRisk(
                "Privileged account compromise",
                "An attacker may gain privileged access.",
                "SECURITY",
                "Security Engineering",
                5,
                4,
                "OPEN"
        );

        mockMvc.perform(post("/api/risks/{riskId}/mitigations", riskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                               {
                                 "description": "Require phishing-resistant MFA",
                                 "effectiveness": 5
                               }
                               """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.effectiveness").value(5));

        mockMvc.perform(get("/api/risks/{riskId}", riskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mitigationCount").value(1))
                .andExpect(jsonPath("$.residualScore").value(10))
                .andExpect(jsonPath("$.residualSeverity").value("MEDIUM"));
    }

    @Test
    void listSupportsDescendingResidualSortAndCategoryAndStatusFilters() throws Exception {
        createRisk(
                "Critical Security Risk",
                "Highest residual score.",
                "SECURITY",
                "Sec",
                5,
                5,
                "OPEN"
        );

        createRisk(
                "Medium Financial Risk",
                "Medium residual score.",
                "FINANCIAL",
                "Finance",
                4,
                4,
                "OPEN"
        );

        String lowerSecurityRiskId = createRisk(
                "Lower Security Risk",
                "Lower residual score.",
                "SECURITY",
                "Sec",
                5,
                4,
                "OPEN"
        );

        addMitigation(
                lowerSecurityRiskId,
                "Strong access control",
                5
        );

        String closedComplianceRiskId = createRisk(
                "Closed Compliance Risk",
                "Closed risk.",
                "COMPLIANCE",
                "Compliance",
                2,
                2,
                "OPEN"
        );

        addMitigation(
                closedComplianceRiskId,
                "Policy control",
                5
        );

        updateRiskStatus(
                closedComplianceRiskId,
                "Closed Compliance Risk",
                "Closed risk.",
                "COMPLIANCE",
                "Compliance",
                2,
                2,
                "CLOSED"
        );

        // Descending residual score.
        mockMvc.perform(get("/api/risks")
                        .param("sort", "residualScore,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Critical Security Risk"))
                .andExpect(jsonPath("$[0].residualScore").value(25))
                .andExpect(jsonPath("$[1].title").value("Medium Financial Risk"))
                .andExpect(jsonPath("$[1].residualScore").value(16))
                .andExpect(jsonPath("$[2].title").value("Lower Security Risk"))
                .andExpect(jsonPath("$[2].residualScore").value(10))
                .andExpect(jsonPath("$[3].title").value("Closed Compliance Risk"))
                .andExpect(jsonPath("$[3].residualScore").value(2));

        // Category filter.
        mockMvc.perform(get("/api/risks")
                        .param("category", "SECURITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("SECURITY"))
                .andExpect(jsonPath("$[1].category").value("SECURITY"));

        // Status filter.
        mockMvc.perform(get("/api/risks")
                        .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Closed Compliance Risk"))
                .andExpect(jsonPath("$[0].status").value("CLOSED"));

        // Ascending residual score.
        mockMvc.perform(get("/api/risks")
                        .param("sort", "residualScore,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Closed Compliance Risk"))
                .andExpect(jsonPath("$[0].residualScore").value(2))
                .andExpect(jsonPath("$[1].title").value("Lower Security Risk"))
                .andExpect(jsonPath("$[1].residualScore").value(10))
                .andExpect(jsonPath("$[2].title").value("Medium Financial Risk"))
                .andExpect(jsonPath("$[2].residualScore").value(16))
                .andExpect(jsonPath("$[3].title").value("Critical Security Risk"))
                .andExpect(jsonPath("$[3].residualScore").value(25));
    }

    @Test
    void updatesMitigationAndRecalculatesResidualScore() throws Exception {
        String riskId = createRisk(
                "Payment fraud",
                "Fraudulent payment activity.",
                "FINANCIAL",
                "Finance",
                4,
                5,
                "OPEN"
        );

        String mitigationId = addMitigation(
                riskId,
                "Manual review",
                2
        );

        mockMvc.perform(get("/api/risks/{riskId}", riskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residualScore").value(16));

        mockMvc.perform(put(
                        "/api/risks/{riskId}/mitigations/{mitigationId}",
                        riskId,
                        mitigationId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Automated payment fraud detection",
                                  "effectiveness": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description")
                        .value("Automated payment fraud detection"))
                .andExpect(jsonPath("$.effectiveness").value(5));

        mockMvc.perform(get("/api/risks/{riskId}", riskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residualScore").value(10));
    }

    @Test
    void deletesMitigationFromOpenRiskAndResidualReturnsToInherent() throws Exception {
        String riskId = createRisk(
                "Data loss",
                "Potential loss of sensitive data.",
                "SECURITY",
                "Security",
                4,
                4,
                "OPEN"
        );

        String mitigationId = addMitigation(
                riskId,
                "Encrypted backups",
                5
        );

        mockMvc.perform(delete(
                        "/api/risks/{riskId}/mitigations/{mitigationId}",
                        riskId,
                        mitigationId
                ))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/risks/{riskId}", riskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mitigationCount").value(0))
                .andExpect(jsonPath("$.residualScore").value(16));
    }

    @Test
    void updatesRiskAndPersistsChangedFields() throws Exception {
        String riskId = createRisk(
                "Initial title",
                "Initial description.",
                "OPERATIONAL",
                "Operations",
                2,
                3,
                "OPEN"
        );

        mockMvc.perform(put("/api/risks/{riskId}", riskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "description": "Updated description.",
                                  "category": "STRATEGIC",
                                  "owner": "Strategy",
                                  "likelihood": 5,
                                  "impact": 4,
                                  "status": "MITIGATING",
                                  "nextReviewDate": "2026-09-15",
                                  "frameworkFunctions": ["GV", "ID"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.category").value("STRATEGIC"))
                .andExpect(jsonPath("$.status").value("MITIGATING"))
                .andExpect(jsonPath("$.likelihood").value(5))
                .andExpect(jsonPath("$.impact").value(4))
                .andExpect(jsonPath("$.nextReviewDate").value("2026-09-15"))
                .andExpect(jsonPath("$.frameworkFunctions")
                        .value(containsInAnyOrder("GV", "ID")));
    }

    @Test
    void deletesRiskAndReturnsNotFoundAfterDeletion() throws Exception {
        String riskId = createRisk(
                "Temporary risk",
                "Will be deleted.",
                "OPERATIONAL",
                "Operations",
                1,
                2,
                "OPEN"
        );

        mockMvc.perform(delete("/api/risks/{riskId}", riskId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/risks/{riskId}", riskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void persistsFrameworkMappingsAndNextReviewDateAndReturnsOverdueFlag() throws Exception {
        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                               {
                                 "title": "Overdue governance risk",
                                 "description": "A test risk for the stretch fields.",
                                 "category": "COMPLIANCE",
                                 "owner": "Compliance",
                                 "likelihood": 4,
                                 "impact": 4,
                                 "status": "MITIGATING",
                                 "nextReviewDate": "2026-08-22",
                                 "frameworkFunctions": ["GV", "ID"]
                               }
                               """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewOverdue").value(true))
                .andExpect(jsonPath("$.nextReviewDate").value("2026-08-22"))
                .andExpect(jsonPath("$.frameworkFunctions")
                        .value(containsInAnyOrder("GV", "ID")));
    }

    @Test
    void rejectsMalformedRiskIdAsABadRequest() throws Exception {
        mockMvc.perform(get("/api/risks/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void rejectsInvalidLikelihoodWithClearFieldError() throws Exception {
        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid likelihood",
                                  "description": "Likelihood must be between 1 and 5.",
                                  "category": "OPERATIONAL",
                                  "owner": "Operations",
                                  "likelihood": 6,
                                  "impact": 4,
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.likelihood")
                        .value("likelihood must be an integer between 1 and 5"));
    }

    @Test
    void rejectsInvalidImpactWithClearFieldError() throws Exception {
        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid impact",
                                  "description": "Impact must be between 1 and 5.",
                                  "category": "OPERATIONAL",
                                  "owner": "Operations",
                                  "likelihood": 3,
                                  "impact": 6,
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.impact")
                        .value("impact must be an integer between 1 and 5"));
    }

    @Test
    void rejectsInvalidMitigationEffectivenessWithClearFieldError() throws Exception {
        String riskId = createRisk(
                "Invalid mitigation test",
                "Test mitigation validation.",
                "OPERATIONAL",
                "Operations",
                3,
                3,
                "OPEN"
        );

        mockMvc.perform(post(
                        "/api/risks/{riskId}/mitigations",
                        riskId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Invalid mitigation",
                                  "effectiveness": 6
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.effectiveness")
                        .value("effectiveness must be an integer between 1 and 5"));
    }

    @Test
    void rejectsFractionalValuesInsteadOfCoercingThemToIntegers() throws Exception {
        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                               {
                                 "title": "Fractional risk",
                                 "description": "Likelihood must be a whole number.",
                                 "category": "OPERATIONAL",
                                 "owner": "Operations",
                                 "likelihood": 2.5,
                                 "impact": 4,
                                 "status": "OPEN"
                               }
                               """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void rejectsClosingRiskWithoutMitigations() throws Exception {
        String riskId = createRisk(
                "Unmitigated risk",
                "Must remain open.",
                "COMPLIANCE",
                "Compliance",
                3,
                3,
                "OPEN"
        );

        mockMvc.perform(put("/api/risks/{riskId}", riskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unmitigated risk",
                                  "description": "Must remain open.",
                                  "category": "COMPLIANCE",
                                  "owner": "Compliance",
                                  "likelihood": 3,
                                  "impact": 3,
                                  "status": "CLOSED"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("RISK_CANNOT_CLOSE_WITHOUT_MITIGATION"));
    }

    @Test
    void preventsDeletingFinalMitigationFromClosedRisk() throws Exception {
        String riskId = createRisk(
                "Closed risk with evidence",
                "The final control must remain attached while closed.",
                "FINANCIAL",
                "Finance",
                2,
                4,
                "OPEN"
        );

        String mitigationId = addMitigation(
                riskId,
                "Automated duplicate-payment detection",
                5
        );

        updateRiskStatus(
                riskId,
                "Closed risk with evidence",
                "The final control must remain attached while closed.",
                "FINANCIAL",
                "Finance",
                2,
                4,
                "CLOSED"
        );

        mockMvc.perform(delete(
                        "/api/risks/{riskId}/mitigations/{mitigationId}",
                        riskId,
                        mitigationId
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("CANNOT_REMOVE_LAST_MITIGATION_FROM_CLOSED_RISK"));
    }

    private String createRisk(
            String title,
            String description,
            String category,
            String owner,
            int likelihood,
            int impact,
            String status
    ) throws Exception {
        String response = mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "%s",
                                  "category": "%s",
                                  "owner": "%s",
                                  "likelihood": %d,
                                  "impact": %d,
                                  "status": "%s"
                                }
                                """.formatted(
                                title,
                                description,
                                category,
                                owner,
                                likelihood,
                                impact,
                                status
                        )))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String addMitigation(
            String riskId,
            String description,
            int effectiveness
    ) throws Exception {
        String response = mockMvc.perform(post(
                        "/api/risks/{riskId}/mitigations",
                        riskId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "%s",
                                  "effectiveness": %d
                                }
                                """.formatted(
                                description,
                                effectiveness
                        )))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private void updateRiskStatus(
            String riskId,
            String title,
            String description,
            String category,
            String owner,
            int likelihood,
            int impact,
            String status
    ) throws Exception {
        mockMvc.perform(put("/api/risks/{riskId}", riskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "%s",
                                  "category": "%s",
                                  "owner": "%s",
                                  "likelihood": %d,
                                  "impact": %d,
                                  "status": "%s"
                                }
                                """.formatted(
                                title,
                                description,
                                category,
                                owner,
                                likelihood,
                                impact,
                                status
                        )))
                .andExpect(status().isOk());
    }
}
