-- V2__add_event_transfers_cost.sql
-- Adds the FPL hit penalty (event_transfers_cost) to rank history for transfer impact analysis.

ALTER TABLE user_team_rank_history ADD COLUMN event_transfers_cost INTEGER NOT NULL DEFAULT 0;
