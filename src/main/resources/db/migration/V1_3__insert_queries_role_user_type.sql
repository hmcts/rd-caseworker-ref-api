-- insert queries for role_type table
INSERT INTO role_type (role_id, description, created_date) VALUES (1, 'senior-tribunal-caseworker',timezone('utc', now()));
INSERT INTO role_type (role_id, description, created_date) VALUES (2, 'tribunal-caseworker',timezone('utc', now()));
-- insert queries for user_type table
INSERT INTO user_type (user_type_id, description, created_date) VALUES (1, 'CTSC',timezone('utc', now()));
INSERT INTO user_type (user_type_id, description, created_date) VALUES (2, 'CTRT', timezone('utc', now()));
INSERT INTO user_type (user_type_id, description, created_date) VALUES (3, 'Legal office', timezone('utc', now()));