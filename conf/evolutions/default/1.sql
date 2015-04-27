# --- !Ups

CREATE TABLE user (
  name      VARCHAR(255)  NOT NULL,
  email     VARCHAR(255)  NOT NULL UNIQUE,
  password  VARCHAR(255)  NOT NULL,
  avatar    VARCHAR(255)  NOT NULL UNIQUE,
  PRIMARY KEY (name)
);

CREATE TABLE token (
  value         VARCHAR(255) NOT NULL,
  player        VARCHAR(255) NOT NULL,
  scope         VARCHAR(255) NOT NULL,
  captured_date TIMESTAMP    NOT NULL,
  PRIMARY KEY (value)
);

# --- !Downs

DROP TABLE token;
DROP TABLE user;