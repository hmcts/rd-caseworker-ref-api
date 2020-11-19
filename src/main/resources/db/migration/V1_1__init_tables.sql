
create schema if not exists dbrdcaseworker;

create table case_worker_profile(
	case_worker_id varchar(64),
	first_name varchar(128),
	last_name varchar(128),
	email_id varchar(512) not null,
	user_type_id int,
	region varchar(64),
	region_id integer not null,
	delete_flag boolean,
	delete_date timestamp,
	created_date timestamp,
	last_update timestamp,
	constraint case_worker_profile_pk primary key (case_worker_id),
	constraint email_id_unique unique (email_id)
);

create table case_worker_location(
	case_worker_location_id bigint,
	case_worker_id varchar(64),
	location varchar(128),
    location_id int not null,
    primary_flag boolean,
	created_date timestamp,
	last_update timestamp,
	constraint case_worker_location_pk primary key (case_worker_location_id),
	constraint case_worker_locn_id_uq unique (case_worker_id,location_id)
);



create table case_worker_work_area(
	case_worker_work_area_id bigint,
	case_worker_id varchar(64) not null,
	area_of_work varchar(128) not null,
    service_code varchar(16) not null,
	created_date timestamp,
	last_update timestamp,
	constraint case_worker_work_area_pk primary key (case_worker_work_area_id),
	constraint case_worker_work_area_uq unique (case_worker_id,service_code)
);

create table case_worker_role(
	case_worker_role_id bigint,
	case_worker_id varchar(64) not null,
	role_id int not null,
	primary_flag boolean,
	created_date timestamp,
	last_update timestamp,
	constraint case_worker_role_pk primary key (case_worker_role_id),
	constraint case_worker_role_id_uq unique (case_worker_id,role_id)
);

create table role_type(
	role_id int,
	description varchar(512),
	created_date timestamp,
	last_update timestamp,
	constraint role_id_pk primary key (role_id)
);

create table user_type(
	user_type_id int,
	description varchar(512),
	created_date timestamp,
	last_update timestamp,
	constraint user_type_id_pk primary key (user_type_id)
);

create table case_worker_idam_role_assoc(
    cw_idam_role_assoc_id bigint,
    role_id int not null,
	service_code varchar(16) not null,
	idam_role varchar(64) not null,
	created_date timestamp,
	last_update timestamp,
	constraint cw_idam_role_assoc_id_pk primary key (cw_idam_role_assoc_id)
);

create table case_worker_audit (
    job_id bigint,
    authenticated_user_id varchar(32),
    job_start_time timestamp not null,
    file_name varchar(64) not null,
    job_end_time timestamp,
    status varchar(32),
    comments varchar(512),
    constraint case_Worker_audit_pk primary key (job_id)
);

create table case_worker_exception (
   id bigint,
   job_id bigint not null,
   excel_row_id varchar(32),
   email_id varchar(32),
   field_in_error varchar(256),
   error_description varchar(512),
   updated_timestamp timestamp,
   constraint case_worker_exception_pk primary key (id)
);

alter table case_worker_location add constraint case_worker_id_fk1 foreign key (case_worker_id)
references case_worker_profile (case_worker_id);

alter table case_worker_work_area add constraint case_worker_id_fk2 foreign key (case_worker_id)
references case_worker_profile (case_worker_id);

alter table case_worker_role add constraint case_worker_id_fk3 foreign key (case_worker_id)
references case_worker_profile (case_worker_id);

alter table case_worker_role add constraint role_id_fk1 foreign key (role_id)
references role_type (role_id);

alter table case_worker_idam_role_assoc add constraint role_id_fk2 foreign key (role_id)
references role_type (role_id);

alter table case_worker_exception add constraint job_id_fk1 foreign key (job_id)
references case_worker_audit (job_id);