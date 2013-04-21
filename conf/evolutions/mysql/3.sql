# --- !Ups

ALTER TABLE player_elo MODIFY COLUMN elo_change DECIMAL(6, 2) NOT NULL;
ALTER TABLE player_elo MODIFY COLUMN elo        DECIMAL(6, 2) NOT NULL;

ALTER TABLE match_detail DROP COLUMN confirmed_by;

# --- !Downs

ALTER TABLE match_detail ADD COLUMN confirmed_by VARCHAR(255);

ALTER TABLE player_elo MODIFY COLUMN elo_change MEDIUMINT NOT NULL;
ALTER TABLE player_elo MODIFY COLUMN elo        MEDIUMINT NOT NULL;