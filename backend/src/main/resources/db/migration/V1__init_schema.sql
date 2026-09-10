-- V1__init_schema.sql
-- Initial schema for FPL Stats application

CREATE TABLE IF NOT EXISTS team (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fpl_id INTEGER NOT NULL UNIQUE,
    code INTEGER NOT NULL DEFAULT 0,
    name VARCHAR(255),
    short_name VARCHAR(10),
    strength INTEGER NOT NULL DEFAULT 0,
    position INTEGER NOT NULL DEFAULT 0,
    played INTEGER NOT NULL DEFAULT 0,
    win INTEGER NOT NULL DEFAULT 0,
    draw INTEGER NOT NULL DEFAULT 0,
    loss INTEGER NOT NULL DEFAULT 0,
    points INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS gameweek (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_week_number INTEGER NOT NULL UNIQUE,
    name VARCHAR(255),
    deadline_time TIMESTAMPTZ,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    is_next BOOLEAN NOT NULL DEFAULT FALSE,
    is_previous BOOLEAN NOT NULL DEFAULT FALSE,
    is_finished BOOLEAN NOT NULL DEFAULT FALSE,
    average_score INTEGER,
    highest_score INTEGER,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS player (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fpl_id INTEGER NOT NULL UNIQUE,
    first_name VARCHAR(255),
    second_name VARCHAR(255),
    web_name VARCHAR(255),
    position VARCHAR(10),
    code INTEGER NOT NULL DEFAULT 0,
    now_cost DOUBLE PRECISION NOT NULL DEFAULT 0,
    total_points INTEGER NOT NULL DEFAULT 0,
    form VARCHAR(20),
    status VARCHAR(5),
    selected_by_percent DOUBLE PRECISION NOT NULL DEFAULT 0,
    team_id UUID REFERENCES team(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_player_fpl_id ON player(fpl_id);
CREATE INDEX idx_player_team_id ON player(team_id);

CREATE TABLE IF NOT EXISTS player_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    points INTEGER NOT NULL DEFAULT 0,
    minutes_played INTEGER NOT NULL DEFAULT 0,
    goals_scored INTEGER NOT NULL DEFAULT 0,
    assists INTEGER NOT NULL DEFAULT 0,
    clean_sheets INTEGER NOT NULL DEFAULT 0,
    yellow_cards INTEGER NOT NULL DEFAULT 0,
    red_cards INTEGER NOT NULL DEFAULT 0,
    bonus INTEGER NOT NULL DEFAULT 0,
    bps INTEGER NOT NULL DEFAULT 0,
    saves INTEGER NOT NULL DEFAULT 0,
    own_goals INTEGER NOT NULL DEFAULT 0,
    penalties_saved INTEGER NOT NULL DEFAULT 0,
    penalties_missed INTEGER NOT NULL DEFAULT 0,
    starts INTEGER NOT NULL DEFAULT 0,
    expected_goals VARCHAR(20),
    expected_assists VARCHAR(20),
    expected_goal_involvements VARCHAR(20),
    goals_conceded INTEGER NOT NULL DEFAULT 0,
    expected_goals_conceded VARCHAR(20),
    clearances_blocks_interceptions INTEGER NOT NULL DEFAULT 0,
    recoveries INTEGER NOT NULL DEFAULT 0,
    tackles INTEGER NOT NULL DEFAULT 0,
    defensive_contribution INTEGER NOT NULL DEFAULT 0,
    influence VARCHAR(20),
    creativity VARCHAR(20),
    threat VARCHAR(20),
    ict_index VARCHAR(20),
    was_home BOOLEAN NOT NULL DEFAULT FALSE,
    value INTEGER NOT NULL DEFAULT 0,
    transfers_in INTEGER NOT NULL DEFAULT 0,
    transfers_out INTEGER NOT NULL DEFAULT 0,
    transfers_balance INTEGER NOT NULL DEFAULT 0,
    selected INTEGER NOT NULL DEFAULT 0,
    player_id UUID NOT NULL REFERENCES player(id),
    gameweek_id UUID NOT NULL REFERENCES gameweek(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (player_id, gameweek_id)
);

CREATE INDEX idx_player_history_player_id ON player_history(player_id);
CREATE INDEX idx_player_history_gameweek_id ON player_history(gameweek_id);

CREATE TABLE IF NOT EXISTS user_team (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fpl_team_id BIGINT NOT NULL UNIQUE,
    player_first_name VARCHAR(255),
    player_last_name VARCHAR(255),
    team_name VARCHAR(255),
    region VARCHAR(255),
    overall_rank INTEGER,
    total_points INTEGER,
    started_event INTEGER,
    last_synced_game_week INTEGER NOT NULL DEFAULT 0,
    team_value DOUBLE PRECISION,
    bank INTEGER,
    total_transfers INTEGER,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_user_team_fpl_team_id ON user_team(fpl_team_id);

CREATE TABLE IF NOT EXISTS user_pick (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    is_captain BOOLEAN NOT NULL DEFAULT FALSE,
    is_vice_captain BOOLEAN NOT NULL DEFAULT FALSE,
    is_triple_captain BOOLEAN NOT NULL DEFAULT FALSE,
    is_benched BOOLEAN NOT NULL DEFAULT FALSE,
    multiplier INTEGER NOT NULL DEFAULT 1,
    position INTEGER NOT NULL DEFAULT 0,
    user_team_id UUID NOT NULL REFERENCES user_team(id),
    gameweek_id UUID NOT NULL REFERENCES gameweek(id),
    player_id UUID NOT NULL REFERENCES player(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (user_team_id, gameweek_id, player_id)
);

CREATE INDEX idx_user_pick_user_team_id ON user_pick(user_team_id);
CREATE INDEX idx_user_pick_gameweek_id ON user_pick(gameweek_id);
CREATE INDEX idx_user_pick_player_id ON user_pick(player_id);

CREATE TABLE IF NOT EXISTS tracked_team (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fpl_team_id BIGINT NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_synced_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_tracked_team_fpl_team_id ON tracked_team(fpl_team_id);
CREATE INDEX idx_tracked_team_active ON tracked_team(active) WHERE active = TRUE;

CREATE TABLE IF NOT EXISTS user_team_rank_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_week INTEGER NOT NULL,
    overall_rank INTEGER NOT NULL,
    gw_points INTEGER NOT NULL DEFAULT 0,
    total_points INTEGER NOT NULL DEFAULT 0,
    gw_rank INTEGER NOT NULL DEFAULT 0,
    bank INTEGER NOT NULL DEFAULT 0,
    team_value DOUBLE PRECISION NOT NULL DEFAULT 0,
    event_transfers INTEGER NOT NULL DEFAULT 0,
    points_on_bench INTEGER NOT NULL DEFAULT 0,
    chip_used VARCHAR(10),
    user_team_id UUID NOT NULL REFERENCES user_team(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (user_team_id, game_week)
);

CREATE INDEX idx_user_team_rank_history_user_team_id ON user_team_rank_history(user_team_id);
