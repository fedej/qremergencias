-- role
INSERT INTO role (id, version, authority, assignable) VALUES (1, 0, 'ROLE_USER', true);
INSERT INTO role (id, version, authority, assignable) VALUES (2, 0, 'ROLE_OPERATOR', true);
INSERT INTO role (id, version, authority, assignable) VALUES (3, 0, 'ROLE_ADMIN', false);

-- user
INSERT INTO user (username, password, enabled, id, account_non_locked, email, account_non_expired, credentials_non_expired, version) VALUES ('admin', '$2a$10$0Nehr1HnQn71lKWv/35/5OjFlfdGHZuLcq/05ovRkZMZiBOl0C/Ua', true, 1, true, 'federico.jaite@gmail.com', true, true, 1);

-- user_roles
INSERT INTO user_roles (roles_id, user_id) VALUES (3, 1);
INSERT INTO user_roles (roles_id, user_id) VALUES (2, 1);