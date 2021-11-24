UPDATE user_type
SET description = 'Future Operations',
last_update = timezone('utc', now())
WHERE user_type_id = 2;

INSERT INTO user_type (user_type_id, description, created_date, last_update) VALUES (4, 'NBC', timezone('utc', now()), timezone('utc', now()));