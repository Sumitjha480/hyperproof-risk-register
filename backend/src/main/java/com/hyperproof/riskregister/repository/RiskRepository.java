package com.hyperproof.riskregister.repository;

import com.hyperproof.riskregister.domain.Risk;
import com.hyperproof.riskregister.domain.RiskCategory;
import com.hyperproof.riskregister.domain.RiskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskRepository extends JpaRepository<Risk, UUID> {

    @Query("""
            select distinct risk
            from Risk risk
            left join fetch risk.mitigations
            where (:category is null or risk.category = :category)
              and (:status is null or risk.status = :status)
            """)
    List<Risk> findAllWithMitigations(
            @Param("category") RiskCategory category,
            @Param("status") RiskStatus status
    );

    @Query("""
            select distinct risk
            from Risk risk
            left join fetch risk.mitigations
            where risk.id = :id
            """)
    Optional<Risk> findByIdWithMitigations(@Param("id") UUID id);
}
