ALTER TABLE judicial_user_profile ALTER COLUMN contract_type DROP NOT NULL;
ALTER TABLE judicial_user_profile ALTER COLUMN work_pattern DROP NOT NULL;
ALTER TABLE judicial_user_profile ALTER COLUMN email_id DROP NOT NULL;
ALTER TABLE judicial_user_profile DROP CONSTRAINT email_id;
ALTER TABLE judicial_office_appointment ALTER COLUMN role_id DROP NOT NULL;