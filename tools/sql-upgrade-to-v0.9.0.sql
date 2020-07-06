--------------------------------------------------------------------------------------------
-- Migrates Eventeum SQL database schema from v0.8.0 to v0.9.0.
--------------------------------------------------------------------------------------------

-- Add "timestamp" column to contract_event_details and transaction_details
ALTER TABLE contract_event_details ADD timestamp numeric(19,2);
ALTER TABLE transaction_details ADD timestamp numeric(19,2);


