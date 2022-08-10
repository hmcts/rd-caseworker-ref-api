-- update queries for role_type table
UPDATE role_type
SET description = 'Senior Legal Caseworker',
last_update = CURRENT_TIMESTAMP
WHERE role_id = 1;

UPDATE role_type
SET description = 'Legal Caseworker',
last_update = CURRENT_TIMESTAMP
WHERE role_id = 2;