-- insert queries for role_type table new business roles 3-10

UPDATE role_type
SET description = 'National Business Centre Team Leader',
last_update =  CURRENT_TIMESTAMP
WHERE role_id = 6;

UPDATE role_type
SET description = 'National Business Centre Listing Team',
last_update =  CURRENT_TIMESTAMP
WHERE role_id = 7;

UPDATE role_type
SET description = 'National Business Centre Payments Team',
last_update =  CURRENT_TIMESTAMP
WHERE role_id = 8;

UPDATE role_type
SET description = 'CTSC Team Leader',
last_update =  CURRENT_TIMESTAMP
WHERE role_id = 9;

--Covers V1_13
INSERT INTO
    role_type (role_id, description, created_date)
VALUES
    (11, 'National Business Centre Administrator',CURRENT_TIMESTAMP),
    (12, 'Regional Centre Team Leader',CURRENT_TIMESTAMP),
    (13, 'Regional Centre Administrator',CURRENT_TIMESTAMP);