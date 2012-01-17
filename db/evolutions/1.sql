# User schema

# --- !Ups

CREATE TABLE user (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX user_name
    ON user (name);

# --- !Downs

DROP TABLE user;