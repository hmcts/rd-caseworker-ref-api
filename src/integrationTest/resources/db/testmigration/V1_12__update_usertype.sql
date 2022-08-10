UPDATE user_type
SET description = 'Future Operations',
last_update = CURRENT_TIMESTAMP
WHERE user_type_id = 2;

INSERT INTO user_type (user_type_id, description, created_date, last_update) VALUES (6, 'NBC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);