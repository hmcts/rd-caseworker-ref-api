CREATE INDEX IF NOT EXISTS idx_skill_service_id ON dbrdcaseworker.skill(service_id);
CREATE INDEX IF NOT EXISTS idx_case_worker_name ON dbrdcaseworker.case_worker_profile (LOWER(first_name), LOWER(last_name));
CREATE INDEX IF NOT EXISTS idx_case_worker_user_type ON dbrdcaseworker.case_worker_profile(user_type_id);
CREATE INDEX IF NOT EXISTS idx_case_worker_work_area ON dbrdcaseworker.case_worker_work_area(case_worker_id, service_code);
CREATE INDEX IF NOT EXISTS idx_case_worker_role ON dbrdcaseworker.case_worker_role(case_worker_id, role_id);
CREATE INDEX IF NOT EXISTS idx_case_worker_location ON dbrdcaseworker.case_worker_location(case_worker_id, location_id);
CREATE INDEX IF NOT EXISTS idx_case_worker_skill ON dbrdcaseworker.case_worker_skill(case_worker_id, skill_id);