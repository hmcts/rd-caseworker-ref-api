-- Staff: Create new tables to support Staff UI
CREATE TABLE case_worker_skill(
case_worker_skill_id bigint,
case_worker_id varchar(64) NOT NULL,
skill_id int NOT NULL,
created_date timestamp,
last_update timestamp,
CONSTRAINT case_worker_skill_pk PRIMARY KEY (case_worker_skill_id)
);

