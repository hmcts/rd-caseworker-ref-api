-- insert queries for role_type table new business roles 3-10
INSERT INTO
    role_type (role_id, description, created_date)
VALUES
    (3, 'Hearing Centre Team Leader',timezone('utc', now())),
    (4, 'Hearing Centre Administrator',timezone('utc', now())),
    (5, 'Court Clerk',timezone('utc', now())),
    (6, 'National Business Centre Team leader',timezone('utc', now())),
    (7, 'National Business Centre Listing team',timezone('utc', now())),
    (8, 'National Business Centre Payments team',timezone('utc', now())),
    (9, 'CTSC team leader',timezone('utc', now())),
    (10, 'CTSC Administrator',timezone('utc', now()));

