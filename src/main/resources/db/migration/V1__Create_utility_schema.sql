CREATE TABLE utility_provider (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    comment VARCHAR(1024),
    due_day INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    default_amount DECIMAL(19, 4)
);

CREATE TABLE bill (
    id UUID PRIMARY KEY,
    provider_id UUID NOT NULL,
    amount DECIMAL(19, 4),
    status VARCHAR(20) NOT NULL,
    due_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_provider FOREIGN KEY (provider_id) REFERENCES utility_provider(id) ON DELETE CASCADE
);

CREATE INDEX idx_bill_due_date ON bill(due_date);
CREATE INDEX idx_bill_provider_id ON bill(provider_id);