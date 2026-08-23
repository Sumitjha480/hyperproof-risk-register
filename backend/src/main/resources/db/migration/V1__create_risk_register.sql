CREATE TABLE risks (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(32) NOT NULL,
    owner VARCHAR(200) NOT NULL,
    likelihood INTEGER NOT NULL,
    impact INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_risks_category CHECK (category IN ('OPERATIONAL', 'FINANCIAL', 'COMPLIANCE', 'SECURITY', 'STRATEGIC')),
    CONSTRAINT chk_risks_likelihood CHECK (likelihood BETWEEN 1 AND 5),
    CONSTRAINT chk_risks_impact CHECK (impact BETWEEN 1 AND 5),
    CONSTRAINT chk_risks_status CHECK (status IN ('OPEN', 'MITIGATING', 'CLOSED'))
);

CREATE TABLE mitigations (
    id UUID PRIMARY KEY,
    risk_id UUID NOT NULL,
    description VARCHAR(2000) NOT NULL,
    effectiveness INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_mitigations_risk FOREIGN KEY (risk_id) REFERENCES risks (id) ON DELETE CASCADE,
    CONSTRAINT chk_mitigations_effectiveness CHECK (effectiveness BETWEEN 1 AND 5)
);

CREATE INDEX idx_risks_category ON risks (category);
CREATE INDEX idx_risks_status ON risks (status);
CREATE INDEX idx_mitigations_risk_id ON mitigations (risk_id);
