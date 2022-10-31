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
    authenticated_user_id varchar(64) NOT NULL,
    request_timestamp timestamp NOT NULL,
    status varchar(32),
    error_description varchar(512),
    case_worker_id varchar(64),
    operation_type varchar(32) NOT NULL,
    request_log json,
    CONSTRAINT staff_audit_pk PRIMARY KEY (id)
);

alter table case_worker_skill add constraint skill_id_fk1 foreign key (skill_id)
references skill (skill_id);

alter table case_worker_skill add constraint case_worker_id_fk4 foreign key (case_worker_id)
references case_worker_profile (case_worker_id);

CREATE SEQUENCE CASE_WORKER_SKILL_ID_SEQ;
ALTER TABLE case_worker_skill ALTER COLUMN case_worker_skill_id
SET DEFAULT nextval('CASE_WORKER_SKILL_ID_SEQ');

create sequence STAFF_AUDIT_ID_SEQ;
ALTER TABLE staff_audit ALTER COLUMN id
SET DEFAULT nextval('STAFF_AUDIT_ID_SEQ');

-- Added skill data
INSERT INTO skill (skill_id,skill_code,description,service_id,user_type,created_date,last_update) VALUES
	 (1,'TEST1','testskill1','ABA5','',NULL,NULL),
	 (2,'TEST2','testskill2','ABA5','',NULL,NULL),
	 (3,'TEST3','testskill3','ABA5','',NULL,NULL),
	 (4,'TEST4','testskill4','ABA5','',NULL,NULL),
	 (5,'TEST1','testskill1','BFA1','',NULL,NULL),
	 (6,'TEST2','testskill2','BFA1','',NULL,NULL),
	 (7,'TEST3','testskill3','BFA1','',NULL,NULL),
	 (8,'TEST4','testskill4','BFA1','',NULL,NULL),
	 (9,'TEST1','testskill1','AAA7','',NULL,NULL),
	 (10,'TEST2','testskill2','AAA7','',NULL,NULL),
	 (11,'TEST3','testskill3','AAA7','',NULL,NULL),
	 (12,'TEST4','testskill4','AAA7','',NULL,NULL),
	 (13,'FRSUBMITTEDCASES','testskill5','ABA2','',NULL,NULL),
	 (14,'FRHWFS','testskill6','ABA2','',NULL,NULL),
	 (15,'PAPERCASESFORMA','testskill7','ABA2','',NULL,NULL),
	 (16,'FREXCEPTIONS','testskill8','ABA2','',NULL,NULL),
	 (17,'SUPPLEMENTARYEVIDENCE','testskill9','ABA2','',NULL,NULL),
	 (18,'FRAPPROVEDORDERS','testskill10','ABA2','',NULL,NULL),
	 (19,'FRREFUSEDORDERS','testskill11','ABA2','',NULL,NULL),
	 (20,'RESPONSERECEIVED','testskill12','ABA2','',NULL,NULL),
	 (21,'INFORECEIVED','testskill13','ABA2','',NULL,NULL),
	 (22,'NEWPAPERCASES','testskill14','ABA2','',NULL,NULL),
	 (23,'AWAITINGINFO','testskill15','ABA2','',NULL,NULL),
	 (24,'AWATINGRESPONSE','testskill16','ABA2','',NULL,NULL),
	 (25,'NEWDIVORCELAW','testskill17','ABA2','',NULL,NULL);


COMMIT;
