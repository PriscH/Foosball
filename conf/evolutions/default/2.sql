# --- !Ups

CREATE TABLE match_detail (
  id            BIGINT(20)    NOT NULL AUTO_INCREMENT,
  captured_date DATETIME      NOT NULL,
  captured_by   VARCHAR(255)  NOT NULL,
  format        VARCHAR(255)  NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (captured_by)  REFERENCES user(name)
);

CREATE TABLE match_result (
  match_id   BIGINT(20)   NOT NULL,
  player     VARCHAR(255) NOT NULL,
  result     VARCHAR(255) NOT NULL,
  rank       MEDIUMINT    NOT NULL,
  game_score MEDIUMINT    NOT NULL,
  goal_score MEDIUMINT    NOT NULL,
  PRIMARY KEY (match_id, player),
  FOREIGN KEY (match_id) REFERENCES match_detail (id),
  FOREIGN KEY (player)   REFERENCES user  (name)
);

CREATE TABLE game (
  id            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  match_id      BIGINT(20)   NOT NULL,
  left_player1  VARCHAR(255) NOT NULL,
  left_player2  VARCHAR(255) NOT NULL,
  right_player1 VARCHAR(255) NOT NULL,
  right_player2 VARCHAR(255) NOT NULL,
  left_score1   MEDIUMINT    NOT NULL,
  left_score2   MEDIUMINT    NOT NULL,
  right_score1  MEDIUMINT    NOT NULL,
  right_score2  MEDIUMINT    NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (match_id)      REFERENCES match_detail (id),
  FOREIGN KEY (left_player1)  REFERENCES user (name),
  FOREIGN KEY (left_player2)  REFERENCES user (name),
  FOREIGN KEY (right_player1) REFERENCES user (name),
  FOREIGN KEY (right_player2) REFERENCES user (name)
);

CREATE TABLE player_elo (
  id            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  player        VARCHAR(255) NOT NULL,
  captured_date DATETIME     NOT NULL,
  match_id      BIGINT(20)   NOT NULL,
  elo_change    DECIMAL(6, 2) NOT NULL,
  elo           DECIMAL(6, 2) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (player)   REFERENCES user (name),
  FOREIGN KEY (match_id) REFERENCES match_detail (id)
);

# --- !Downs

DROP TABLE player_elo;
DROP TABLE game;
DROP TABLE match_result;
DROP TABLE match_detail;