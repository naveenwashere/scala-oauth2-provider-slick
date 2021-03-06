# --- !Ups

CREATE TABLE account (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  email VARCHAR(500) NOT NULL,
  PASSWORD VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY email (email)
) DEFAULT CHARSET=utf8;

INSERT INTO account (email, PASSWORD)
VALUES ('bob@example.com', '48181acd22b3edaebc8a447868a7df7ce629920a');
INSERT INTO account (email, PASSWORD)
VALUES ('alice@example.com', '522b276a356bdf39013dfabea2cd43e141ecc9e8');

CREATE TABLE oauth_client (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  owner_id BIGINT(20) NOT NULL,
  grant_type VARCHAR(20) NOT NULL,
  client_id VARCHAR(100) NOT NULL,
  client_secret VARCHAR(100) NOT NULL,
  redirect_uri VARCHAR(2000) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY oauth_client_client_id_key (client_id),
  KEY oauth_client_owner_id_fkey (owner_id),
  CONSTRAINT FOREIGN KEY (owner_id) REFERENCES account (id) ON DELETE CASCADE
) DEFAULT CHARSET=utf8;

INSERT INTO oauth_client(owner_id, grant_type, client_id, client_secret)
  VALUES (1, 'client_credentials', 'bob_client_id', 'bob_client_secret');
INSERT INTO oauth_client(owner_id, grant_type, client_id, client_secret, redirect_uri)
  VALUES (2, 'authorization_code', 'alice_client_id', 'alice_client_secret', 'http://localhost:3000/callback');
INSERT INTO oauth_client(owner_id, grant_type, client_id, client_secret)
  VALUES (2, 'password', 'alice_client_id2', 'alice_client_secret2');

CREATE TABLE oauth_access_token (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  account_id BIGINT(20) NOT NULL,
  oauth_client_id BIGINT(20) NOT NULL,
  access_token VARCHAR(100) NOT NULL,
  refresh_token VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY oauth_access_token_account_id_fkey (account_id),
  KEY oauth_access_token_oauth_client_id_fkey (oauth_client_id),
  CONSTRAINT oauth_access_token_account_id_fkey FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE,
  CONSTRAINT oauth_access_token_oauth_client_id_fkey FOREIGN KEY (oauth_client_id) REFERENCES oauth_client (id) ON DELETE CASCADE
) DEFAULT CHARSET=utf8;

CREATE TABLE oauth_authorization_code
(
  id BIGINT(20) NOT NULL auto_increment,
  account_id BIGINT(20) NOT NULL,
  oauth_client_id BIGINT(20) NOT NULL,
  code VARCHAR(100) NOT NULL,
  redirect_uri VARCHAR(2000) NOT NULL,
  created_at TIMESTAMP NOT NULL default CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (account_id) REFERENCES account (id) on delete cascade,
  CONSTRAINT FOREIGN KEY (oauth_client_id) REFERENCES oauth_client (id) on delete cascade
);

INSERT INTO oauth_authorization_code(account_id, oauth_client_id, code, redirect_uri)
VALUES (1, 2, 'bob_code', 'http://localhost:3000/callback');