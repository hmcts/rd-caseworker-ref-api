
create sequence CASE_WORKER_LOCATION_ID_SEQ;
create sequence CASE_WORKER_WORK_AREA_ID_SEQ;
create sequence CASE_WORKER_ROLE_ID_SEQ;
create sequence CW_IDAM_ROLE_ASSOC_ID_SEQ;
create sequence JOB_ID_SEQ;
create sequence EXCEPTION_ID_SEQ;

ALTER TABLE case_worker_location ALTER COLUMN case_worker_location_id
SET DEFAULT nextval('CASE_WORKER_LOCATION_ID_SEQ');

ALTER TABLE case_worker_work_area ALTER COLUMN case_worker_work_area_id
SET DEFAULT nextval('CASE_WORKER_WORK_AREA_ID_SEQ');

ALTER TABLE case_worker_role ALTER COLUMN case_worker_role_id
SET DEFAULT nextval('CASE_WORKER_ROLE_ID_SEQ');

ALTER TABLE case_worker_idam_role_assoc ALTER COLUMN cw_idam_role_assoc_id
SET DEFAULT nextval('CW_IDAM_ROLE_ASSOC_ID_SEQ');

ALTER TABLE case_worker_audit ALTER COLUMN job_id
SET DEFAULT nextval('JOB_ID_SEQ');

ALTER TABLE case_worker_exception ALTER COLUMN id
SET DEFAULT nextval('EXCEPTION_ID_SEQ');