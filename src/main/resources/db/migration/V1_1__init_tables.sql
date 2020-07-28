-- NB Flyway requires lowercase for table names
create schema if not exists dbrdcaseworker;

CREATE TABLE judicial_user_profile(
	elinks_Id varchar(256) NOT NULL,
	personal_code varchar(32) NOT NULL,
	title varchar(64) NOT NULL,
	known_as varchar(64) NOT NULL,
	surname varchar(256) NOT NULL,
	full_name varchar(256) NOT NULL,
	post_nominals varchar(32),
	contract_type varchar(32) NOT NULL,
	work_pattern varchar(32) NOT NULL,
	email_Id varchar(256) NOT NULL,
	joining_date date,
	last_working_date date,
	active_flag boolean,
	extracted_date timestamp NOT NULL,
	created_date timestamp,
	last_loaded_date timestamp,
	CONSTRAINT personal_code_unique UNIQUE (personal_code),
	CONSTRAINT email_Id UNIQUE (email_Id),
	CONSTRAINT elinks_Id PRIMARY KEY (elinks_Id)

);

CREATE TABLE judicial_office_appointment(
	judicial_office_appointment_Id bigint NOT NULL,
	elinks_Id varchar(256) NOT NULL,
	role_id varchar(128) NOT NULL,
	contract_type_Id varchar(256),
	base_location_Id varchar(256),
	region_Id varchar(256),
	is_prinicple_appointment boolean,
	start_date date,
	end_date date,
	active_flag boolean,
	extracted_date timestamp NOT NULL,
	created_date timestamp,
	last_loaded_date timestamp,
	CONSTRAINT judicial_office_appointment_Id PRIMARY KEY (judicial_office_appointment_Id)

);

CREATE TABLE judicial_office_authorisation(
	judicial_office_auth_Id bigint NOT NULL,
	elinks_Id varchar(256) NOT NULL,
	authorisation_Id varchar(256),
	jurisdiction_id varchar(256) NOT NULL,
	authorisation_date date,
	extracted_date date NOT NULL,
	created_date timestamp,
	last_loaded_date timestamp,
	CONSTRAINT jud_auth_pk PRIMARY KEY (judicial_office_auth_Id),
	CONSTRAINT jud_auth_jur_unique UNIQUE (jurisdiction_id)

);
CREATE TABLE authorisation_type(
	authorisation_Id varchar(64) NOT NULL,
	authorisation_desc_en varchar(256) NOT NULL,
	authorisation_desc_cy varchar(256),
	jurisdiction_Id varchar(64),
	jurisdiction_desc_en varchar(256),
	jurisdiction_desc_cy varchar(256),
	CONSTRAINT authorisation_Id PRIMARY KEY (authorisation_Id)

);

CREATE TABLE judicial_role_type(
	role_id varchar(64) NOT NULL,
	role_desc_en varchar(256) NOT NULL,
	role_desc_cy varchar(256),
	CONSTRAINT role_id PRIMARY KEY (role_id)

);

CREATE TABLE contract_type(
	contract_type_Id varchar(64) NOT NULL,
	contract_type_desc_en varchar(256) NOT NULL,
	contract_type_desc_cy varchar(256),
	CONSTRAINT contract_type_Id PRIMARY KEY (contract_type_Id)

);

CREATE TABLE base_location_type(
	base_location_Id varchar(64) NOT NULL,
	court_name varchar(128),
	bench varchar(128),
	court_type varchar(128),
	circuit varchar(128),
	area_of_expertise varchar(128),
	national_court_code varchar(128),
	CONSTRAINT base_location_Id PRIMARY KEY (base_location_Id)

);

CREATE TABLE region_type(
	region_Id varchar(64) NOT NULL,
	region_desc_en varchar(256) NOT NULL,
	region_desc_cy varchar(256),
	CONSTRAINT region_Id PRIMARY KEY (region_Id)

);

ALTER TABLE judicial_office_appointment ADD CONSTRAINT elinks_Id_fk1 FOREIGN KEY (elinks_Id)
REFERENCES judicial_user_profile (elinks_Id);

ALTER TABLE judicial_office_appointment ADD CONSTRAINT role_id_fk1 FOREIGN KEY (role_id)
REFERENCES judicial_role_type (role_id);

ALTER TABLE judicial_office_appointment ADD CONSTRAINT contract_type_Id_fk1 FOREIGN KEY (contract_type_Id)
REFERENCES contract_type (contract_type_Id);

ALTER TABLE judicial_office_appointment ADD CONSTRAINT base_location_Id_fk1 FOREIGN KEY (base_location_Id)
REFERENCES base_location_type (base_location_Id);

ALTER TABLE judicial_office_appointment ADD CONSTRAINT region_Id_fk1 FOREIGN KEY (region_Id)
REFERENCES region_type (region_Id);

ALTER TABLE judicial_office_authorisation ADD CONSTRAINT elinks_Id_fk2 FOREIGN KEY (elinks_Id)
REFERENCES judicial_user_profile (elinks_Id);

ALTER TABLE judicial_office_authorisation ADD CONSTRAINT authorisation_Id_fk1 FOREIGN KEY (authorisation_Id)
REFERENCES authorisation_type (authorisation_Id);
