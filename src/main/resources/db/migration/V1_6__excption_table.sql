CREATE TABLE dataload_exception_records(
                                           id SERIAL NOT NULL,
                                           file_name varchar(256),
                                           table_Name varchar(64),
                                           scheduler_start_time timestamp NOT NULL,
                                           scheduler_name varchar(64) NOT NULL,
                                           key varchar(256),
                                           field_in_error varchar(256),
                                           error_description varchar(512),
                                           updated_timestamp timestamp NOT NULL,
                                           CONSTRAINT dataload_exception_records_pk PRIMARY KEY (ID)
);







