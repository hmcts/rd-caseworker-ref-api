-- insert queries for role_type table
INSERT INTO role_type (role_id, description, created_date) VALUES (1, 'SENIOR_TRIBUNAL_CASEWORKER',CURRENT_TIMESTAMP);
INSERT INTO role_type (role_id, description, created_date) VALUES (2, 'TRIBUNAL_CASEWORKER',CURRENT_TIMESTAMP);
-- insert queries for user_type table
INSERT INTO user_type (user_type_id, description, created_date) VALUES (1, 'CTSC',CURRENT_TIMESTAMP);
INSERT INTO user_type (user_type_id, description, created_date) VALUES (2, 'CTRT', CURRENT_TIMESTAMP);
INSERT INTO user_type (user_type_id, description, created_date) VALUES (3, 'Legal office', CURRENT_TIMESTAMP);