# --- !Ups

ALTER TABLE player_elo ALTER COLUMN elo_change DECIMAL(6, 2) NOT NULL;
ALTER TABLE player_elo ALTER COLUMN elo        DECIMAL(6, 2) NOT NULL;

# --- !Downs

ALTER TABLE player_elo ALTER COLUMN elo_change MEDIUMINT NOT NULL;
ALTER TABLE player_elo ALTER COLUMN elo        MEDIUMINT NOT NULL;