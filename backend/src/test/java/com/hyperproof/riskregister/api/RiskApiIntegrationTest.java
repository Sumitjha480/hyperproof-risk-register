package com.hyperproof.riskregister.api;

import com.fasterxml.jackson.databind.JsonNode;
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
        String createResponse = mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Privileged account compromise",
                                  "description": "An attacker may gain privileged access.",
                                  "category": "SECURITY",
                                  "owner": "Security Engineering",
                                  "likelihood": 5,
                                  "impact": 4,
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inherentScore").value(20))
                .andExpect(jsonPath("$.residualScore").value(20))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(createResponse);
        String riskId = created.get("id").asText();

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
    void rejectsMalformedRiskIdAsABadRequest() throws Exception {
        mockMvc.perform(get("/api/risks/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void rejectsInvalidScaleWithClearFieldError() throws Exception {
        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid risk",
                                  "description": "Invalid likelihood.",
                                  "category": "OPERATIONAL",
                                  "owner": "Operations",
                                  "likelihood": 6,
                                  "impact": 4,
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.likelihood").value("likelihood must be an integer between 1 and 5"));
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
    void preventsClosingRiskWithoutMitigations() throws Exception {
        String response = mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unmitigated risk",
                                  "description": "Must remain open.",
                                  "category": "COMPLIANCE",
                                  "owner": "Compliance",
                                  "likelihood": 3,
                                  "impact": 3,
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String riskId = objectMapper.readTree(response).get("id").asText();

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
                .andExpect(jsonPath("$.code").value("RISK_CANNOT_CLOSE_WITHOUT_MITIGATION"));
    }

    @Test
    void preventsDeletingFinalMitigationFromClosedRisk() throws Exception {
        String createResponse = mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Closed risk with evidence",
                                  "description": "The final control must remain attached while closed.",
                                  "category": "FINANCIAL",
                                  "owner": "Finance",
                                  "likelihood": 2,
                                  "impact": 4,
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String riskId = objectMapper.readTree(createResponse).get("id").asText();

        String mitigationResponse = mockMvc.perform(post("/api/risks/{riskId}/mitigations", riskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Automated duplicate-payment detection",
                                  "effectiveness": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String mitigationId = objectMapper.readTree(mitigationResponse).get("id").asText();

        mockMvc.perform(put("/api/risks/{riskId}", riskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Closed risk with evidence",
                                  "description": "The final control must remain attached while closed.",
                                  "category": "FINANCIAL",
                                  "owner": "Finance",
                                  "likelihood": 2,
                                  "impact": 4,
                                  "status": "CLOSED"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/risks/{riskId}/mitigations/{mitigationId}", riskId, mitigationId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CANNOT_REMOVE_LAST_MITIGATION_FROM_CLOSED_RISK"));
    }

}
