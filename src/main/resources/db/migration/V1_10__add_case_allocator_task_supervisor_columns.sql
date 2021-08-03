ALTER TABLE case_worker_profile
ADD COLUMN case_allocator boolean DEFAULT 'N',
ADD COLUMN task_supervisor boolean DEFAULT 'N';