--------------------------------------------------------------------------------------------
-- Migrates Eventeum Postgres database schema from v0.7.0 to v0.8.0.
--------------------------------------------------------------------------------------------

-- change "type" column to VARCHAR
ALTER TABLE contract_event_filter_indexed_parameter_definitions ALTER COLUMN "type" SET DATA TYPE VARCHAR;
ALTER TABLE contract_event_filter_non_indexed_parameter_definitions ALTER COLUMN "type" SET DATA TYPE VARCHAR;

-- migrate old numeric type values to string identifiers
UPDATE contract_event_filter_indexed_parameter_definitions SET type='INT256' WHERE type='0';
UPDATE contract_event_filter_indexed_parameter_definitions SET type='UINT8' WHERE type='1';
UPDATE contract_event_filter_indexed_parameter_definitions SET type='UINT256' WHERE type='2';
UPDATE contract_event_filter_indexed_parameter_definitions SET type='ADDRESS' WHERE type='3';
UPDATE contract_event_filter_indexed_parameter_definitions SET type='BYTES16' WHERE type='4';
UPDATE contract_event_filter_indexed_parameter_definitions SET type='BYTES32' WHERE type='5';
UPDATE contract_event_filter_indexed_parameter_definitions SET type='BOOL' WHERE type='6';
UPDATE contract_event_filter_indexed_parameter_definitions SET type='STRING' WHERE type='7';

UPDATE contract_event_filter_non_indexed_parameter_definitions SET type='INT256' WHERE type='0';
UPDATE contract_event_filter_non_indexed_parameter_definitions SET type='UINT8' WHERE type='1';
UPDATE contract_event_filter_non_indexed_parameter_definitions SET type='UINT256' WHERE type='2';
UPDATE contract_event_filter_non_indexed_parameter_definitions SET type='ADDRESS' WHERE type='3';
UPDATE contract_event_filter_non_indexed_parameter_definitions SET type='BYTES16' WHERE type='4';
UPDATE contract_event_filter_non_indexed_parameter_definitions SET type='BYTES32' WHERE type='5';
UPDATE contract_event_filter_non_indexed_parameter_definitions SET type='BOOL' WHERE type='6';
UPDATE contract_event_filter_non_indexed_parameter_definitions SET type='STRING' WHERE type='7';

