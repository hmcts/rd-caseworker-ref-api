-- RDCC-2079: rename DeleteFlag to Suspended
ALTER TABLE case_worker_profile RENAME COLUMN delete_flag TO suspended;