CREATE TABLE dataload_schedular_audit(
  id serial NOT NULL,
  scheduler_name varchar(64) NOT NULL,
  scheduler_start_time timestamp NOT NULL,
  scheduler_end_time timestamp,
  scheduler_status varchar(32),
  CONSTRAINT dataload_schedular_audit_pk PRIMARY KEY (id)
);



