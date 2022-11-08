
INSERT INTO
    role_type (role_id, description, created_date)
VALUES
    (14, 'DWP Caseworker',timezone('utc', now())),
    (15, 'HMRC Caseworker',timezone('utc', now()));

INSERT INTO user_type (user_type_id, description, created_date, last_update) VALUES (5, 'Other Government Department', timezone('utc', now()), timezone('utc', now()));