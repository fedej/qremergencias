CREATE TABLE user
(
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255),
    enabled BIT,
    id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    account_non_locked BIT,
    email VARCHAR(255) NOT NULL,
    account_non_expired BIT NOT NULL,
    credentials_non_expired BIT NOT NULL,
    version BIGINT NOT NULL
) ENGINE=InnoDB ;
CREATE UNIQUE INDEX uk_username ON user (username);

CREATE TABLE forgot_password
(
    id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    expiration_time DATETIME NOT NULL,
    expired BIT,
    token VARCHAR(255),
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user (id)
) ENGINE=InnoDB ;

CREATE TABLE password_change
(
    id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    change_date DATETIME NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user (id)
)ENGINE=InnoDB ;

CREATE TABLE role
(
    id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    authority VARCHAR(255) NOT NULL,
    assignable BIT DEFAULT b'1' NOT NULL
)ENGINE=InnoDB ;
CREATE UNIQUE INDEX UK_irsamgnera6angm0prq1kemt2 ON role (authority);

CREATE TABLE user_front
(
    name VARCHAR(255),
    lastname VARCHAR(255),
    id BIGINT PRIMARY KEY NOT NULL,
    FOREIGN KEY (id) REFERENCES user (id)
)ENGINE=InnoDB ;

CREATE TABLE user_roles
(
    roles_id BIGINT DEFAULT 0 NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (roles_id, user_id),
    FOREIGN KEY (roles_id) REFERENCES role (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
)ENGINE=InnoDB ;

CREATE TABLE UserConnection
(
    userId VARCHAR(255) NOT NULL,
    providerId VARCHAR(255) NOT NULL,
    providerUserId VARCHAR(255) DEFAULT '' NOT NULL,
    rank INT NOT NULL,
    displayName VARCHAR(255),
    profileUrl VARCHAR(512),
    imageUrl VARCHAR(512),
    accessToken VARCHAR(512) NOT NULL,
    secret VARCHAR(512),
    refreshToken VARCHAR(512),
    expireTime BIGINT,
    PRIMARY KEY (userId, providerId, providerUserId)
)ENGINE=InnoDB ;

CREATE UNIQUE INDEX UserConnectionRank ON UserConnection (userId, providerId, rank);