# --- !Ups

CREATE TABLE match (
  id            BIGINT(20)    NOT NULL AUTO_INCREMENT,
  match_type    VARCHAR(255)  NOT NULL,
  created_by    BIGINT(20)    NOT NULL,
  created_date  TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE match_result (
  match_id  BIGINT(20)  NOT NULL,
  user_id   BIGINT(20)  NOT NULL,
  rank      TINYINT     NOT NULL,
  score     TINYINT,
  PRIMARY KEY (match_id, user_id),
  FOREIGN KEY (match_id) REFERENCES match (id),
  FOREIGN KEY (user_id)  REFERENCES user  (id)
);

# --- !Downs

DROP TABLE match_result;
DROP TABLE match;