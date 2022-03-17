-- insert queries for role_type table new business roles 3-10

INSERT INTO
    role_type (role_id, description, created_date)
VALUES
    (14, 'DWP Administrator',CURRENT_TIMESTAMP),
    (15, 'HMRC Administrator',CURRENT_TIMESTAMP);

INSERT INTO user_type (user_type_id, description, created_date) VALUES (5, 'Other Government Department',CURRENT_TIMESTAMP);