ALTER TABLE case_worker_profile
ADD case_allocator boolean DEFAULT 0;

ALTER TABLE case_worker_profile
ADD task_supervisor boolean DEFAULT 0;