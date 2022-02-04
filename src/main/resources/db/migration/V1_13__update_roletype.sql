UPDATE role_type
SET description = 'National Business Centre Team Leader',
last_update = timezone('utc', now())
WHERE role_id = 6;

UPDATE role_type
SET description = 'National Business Centre Listing Team',
last_update = timezone('utc', now())
WHERE role_id = 7;

UPDATE role_type
SET description = 'National Business Centre Payments Team',
last_update = timezone('utc', now())
WHERE role_id = 8;

INSERT INTO
    role_type (role_id, description, created_date)
VALUES
    (11, 'National Business Centre Administrator',timezone('utc', now())),
    (12, 'Regional Centre Team Leader',timezone('utc', now())),
    (13, 'Regional Centre Administrator',timezone('utc', now()));