-- Staff: Create new tables to support Staff UI
CREATE TABLE case_worker_skill(
    case_worker_skill_id bigint,
    case_worker_id varchar(64) NOT NULL,
    skill_id int NOT NULL,
    created_date timestamp,
    last_update timestamp,
    CONSTRAINT case_worker_skill_pk PRIMARY KEY (case_worker_skill_id)
);

CREATE TABLE skill(
    skill_id int NOT NULL,
    skill_code varchar(64) NOT NULL,
    description varchar(512) NOT NULL,
    service_id varchar(64) NOT NULL,
    user_type varchar(512) NOT NULL,
    created_date timestamp,
    last_update timestamp,
    CONSTRAINT skill_id_pk PRIMARY KEY (skill_id)
);

ALTER TABLE case_worker_profile
ADD COLUMN user_admin boolean DEFAULT 'N';

CREATE TABLE staff_audit (
    id bigint,
    authenticated_user_id varchar(32) NOT NULL,
    request_timestamp timestamp NOT NULL,
    status varchar(32),
    error_description varchar(512),
    case_worker_id varchar(64),
    operation_type varchar(32) NOT NULL,
    request_log json,
    CONSTRAINT staff_audit_pk PRIMARY KEY (id)
);

COMMIT;


--dummy data
INSERT INTO skill (skill_id, skill_code, description, service_id, user_type, created_date, last_update) VALUES(1, '1', 'testskill1', '1', '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO skill (skill_id, skill_code, description, service_id, user_type, created_date, last_update) VALUES(2, ‘2’, 'testskill2’, ‘2’, ‘2’, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO skill (skill_id, skill_code, description, service_id, user_type, created_date, last_update) VALUES(3, ‘3’, 'testskill3’, ‘3’, ‘3’, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO skill (skill_id, skill_code, description, service_id, user_type, created_date, last_update) VALUES(4, ‘4’, 'testskill4’, ‘4’, ‘4’, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO skill (skill_id, skill_code, description, service_id, user_type, created_date, last_update) VALUES(5, ‘5’, 'testskill5’, ‘5’, ‘5’, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
