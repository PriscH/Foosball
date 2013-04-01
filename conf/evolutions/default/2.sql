# --- !Ups

CREATE TABLE match (
  id            BIGINT(20)    NOT NULL AUTO_INCREMENT,
  captured_date TIMESTAMP     NOT NULL,
  captured_by   VARCHAR(255)  NOT NULL,
  format        VARCHAR(255)  NOT NULL,
  confirmed_by  VARCHAR(255),
  PRIMARY KEY (id),
  FOREIGN KEY (captured_by)  REFERENCES user(name),
  FOREIGN KEY (confirmed_by) REFERENCES user(name)
);

CREATE TABLE match_result (
  match_id  BIGINT(20)   NOT NULL,
  player    VARCHAR(255) NOT NULL,
  result    VARCHAR(255) NOT NULL,
  rank      MEDIUMINT    NOT NULL,
  score     MEDIUMINT    NOT NULL,
  PRIMARY KEY (match_id, player),
  FOREIGN KEY (match_id) REFERENCES match (id),
  FOREIGN KEY (player)   REFERENCES user  (name)
);

CREATE TABLE game (
  id         BIGINT(20)   NOT NULL AUTO_INCREMENT,
  match_id   BIGINT(20)   NOT NULL,
  winner1    VARCHAR(255) NOT NULL,
  winner2    VARCHAR(255) NOT NULL,
  loser1     VARCHAR(255) NOT NULL,
  loser2     VARCHAR(255) NOT NULL,
  result     VARCHAR(255) NOT NULL,
  PRIMARY KEY (id, match_id),
  FOREIGN KEY (match_id) REFERENCES match (id),
  FOREIGN KEY (winner1)  REFERENCES user (name),
  FOREIGN KEY (winner2)  REFERENCES user (name),
  FOREIGN KEY (loser1)   REFERENCES user (name),
  FOREIGN KEY (loser2)   REFERENCES user (name),
);

CREATE TABLE player_elo (
  id            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  player        VARCHAR(255) NOT NULL,
  captured_date TIMESTAMP    NOT NULL,
  match_id      BIGINT(20)   NOT NULL,
  change        MEDIUMINT    NOT NULL,
  elo           MEDIUMINT    NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (player)   REFERENCES user (name),
  FOREIGN KEY (match_id) REFERENCES match (id)
);

# --- !Downs

DROP TABLE player_elo;
DROP TABLE game;
DROP TABLE match_result;
DROP TABLE match;