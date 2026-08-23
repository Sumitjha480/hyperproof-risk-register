package com.hyperproof.riskregister.config;

import com.hyperproof.riskregister.domain.Mitigation;
import com.hyperproof.riskregister.domain.Risk;
import com.hyperproof.riskregister.domain.RiskCategory;
import com.hyperproof.riskregister.domain.RiskStatus;
import com.hyperproof.riskregister.repository.RiskRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
    ApplicationRunner seedDemoRisks(RiskRepository riskRepository) {
        return args -> {
            if (riskRepository.count() > 0) {
                return;
            }

            Risk vendorOutage = new Risk(
                    "Critical vendor service outage",
                    "A prolonged outage at a key infrastructure vendor could interrupt customer-facing services.",
                    RiskCategory.OPERATIONAL,
                    "Platform Operations",
                    4,
                    4,
                    RiskStatus.OPEN
            );

            Risk dataExposure = new Risk(
                    "Customer data exposure",
                    "Unauthorized access to customer data caused by compromised credentials or misconfiguration.",
                    RiskCategory.SECURITY,
                    "Security Engineering",
                    5,
                    5,
                    RiskStatus.MITIGATING
            );
            dataExposure.addMitigation(new Mitigation("Require phishing-resistant MFA for privileged access", 4));
            dataExposure.addMitigation(new Mitigation("Encrypt sensitive customer data at rest and in transit", 5));

            Risk regulatoryGap = new Risk(
                    "Regulatory reporting gap",
                    "A required compliance report may be incomplete or submitted after the filing deadline.",
                    RiskCategory.COMPLIANCE,
                    "Compliance Operations",
                    5,
                    4,
                    RiskStatus.MITIGATING
            );
            regulatoryGap.addMitigation(new Mitigation("Introduce a documented pre-submission review checklist", 2));

            Risk duplicatePayments = new Risk(
                    "Duplicate supplier payments",
                    "Manual payment processing could result in duplicate supplier disbursements.",
                    RiskCategory.FINANCIAL,
                    "Finance Systems",
                    3,
                    3,
                    RiskStatus.CLOSED
            );
            duplicatePayments.addMitigation(new Mitigation("Automated duplicate-invoice detection before payment release", 5));

            riskRepository.saveAll(java.util.List.of(vendorOutage, dataExposure, regulatoryGap, duplicatePayments));
        };
    }
}
