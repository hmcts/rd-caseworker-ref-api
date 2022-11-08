-- update queries for role_type table
UPDATE role_type
SET description = 'Senior Tribunal Caseworker',
last_update = timezone('utc', now())
WHERE role_id = 1;

UPDATE role_type
SET description = 'Tribunal Caseworker',
last_update = timezone('utc', now())
WHERE role_id = 2;