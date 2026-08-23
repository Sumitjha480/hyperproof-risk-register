ALTER TABLE risks ADD COLUMN next_review_date DATE;

CREATE TABLE risk_framework_mappings (
   risk_id UUID NOT NULL,
   framework_function VARCHAR(8) NOT NULL,
   PRIMARY KEY (risk_id, framework_function),
   CONSTRAINT fk_risk_framework_risk FOREIGN KEY (risk_id) REFERENCES risks (id) ON DELETE CASCADE,
   CONSTRAINT chk_risk_framework_function CHECK (framework_function IN ('GV', 'ID', 'PR', 'DE', 'RS', 'RC'))
);

CREATE INDEX idx_risks_next_review_date ON risks (next_review_date);

