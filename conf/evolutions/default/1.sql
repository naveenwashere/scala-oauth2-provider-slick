# --- !Ups

CREATE TABLE account (
  id int(11) NOT NULL AUTO_INCREMENT,
  email varchar(500) NOT NULL,
  PASSWORD varchar(100) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY email (email)
) DEFAULT CHARSET=utf8;

INSERT INTO account (email, PASSWORD)
VALUES
	('naveenwashere@yahoo.com','c6e2f08f1da18c37fc099eeb248f0f3408051503');

CREATE TABLE oauth_client (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  owner_id int(11) NOT NULL,
  grant_type varchar(20) NOT NULL,
  client_id varchar(100) NOT NULL,
  client_secret varchar(100) NOT NULL,
  redirect_uri varchar(2000) DEFAULT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY oauth_client_client_id_key (client_id),
  KEY oauth_client_owner_id_fkey (owner_id),
  CONSTRAINT FOREIGN KEY (owner_id) REFERENCES account (id) ON DELETE CASCADE
) DEFAULT CHARSET=utf8;

INSERT INTO oauth_client (owner_id, grant_type, client_id, client_secret, redirect_uri)
VALUES
	(1,'password','89b8f0ba-9de3-4464-a005-8fb9deb62061','JUoeOv7jEZYm-iBPgBAXHA','');

CREATE TABLE oauth_access_token (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  account_id int(11) NOT NULL,
  oauth_client_id varchar(100) NOT NULL,
  access_token varchar(100) NOT NULL,
  refresh_token varchar(100) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY oauth_access_token_account_id_fkey (account_id),
  KEY oauth_access_token_oauth_client_id_fkey (oauth_client_id),
  CONSTRAINT oauth_access_token_account_id_fkey FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE,
  CONSTRAINT oauth_access_token_oauth_client_id_fkey FOREIGN KEY (oauth_client_id) REFERENCES oauth_client (client_id) ON DELETE CASCADE
) DEFAULT CHARSET=utf8;
