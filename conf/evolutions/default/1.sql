# --- !Ups

CREATE TABLE user (
  name      VARCHAR(255)  NOT NULL,
  password  VARCHAR(255)  NOT NULL,
  avatar    VARCHAR(255)  NOT NULL,
  PRIMARY KEY (name)
);

# --- !Downs

DROP TABLE user;