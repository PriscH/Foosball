# --- !Ups

CREATE TABLE user (
  name      VARCHAR(255)  NOT NULL,
  email     VARCHAR(255)  NOT NULL,
  password  VARCHAR(255)  NOT NULL,
  avatar    VARCHAR(255)  NOT NULL,
  PRIMARY KEY (name)
);

CREATE TABLE token (
  value         VARCHAR(255) NOT NULL,
  scope         VARCHAR(255) NOT NULL,
  captured_date TIMESTAMP    NOT NULL,
  PRIMARY KEY (value)
);

# --- !Downs

DROP TABLE token;
DROP TABLE user;