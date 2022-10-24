-- schema creation
CREATE SCHEMA IF NOT EXISTS rdstaffreport;

-- view creation for staff user profile
create view rdstaffreport.vw_case_worker_profile as
select case_worker_id, first_name, last_name, email_id, user_type_id, region, region_id, case_allocator, task_supervisor, suspended, user_admin
from dbrdcaseworker.case_worker_profile;

-- view creation for case worker location
create view rdstaffreport.vw_case_worker_location as
select case_worker_id, location,location_id
from dbrdcaseworker.case_worker_location;

-- view creation for Case worker services
create view rdstaffreport.vw_case_worker_work_area as
select case_worker_id, area_of_work, service_code
from dbrdcaseworker.case_worker_work_area;

-- view creation for Case worker roles
create view rdstaffreport.vw_case_worker_role as
select case_worker_id, role_id
from dbrdcaseworker.case_worker_role;

-- view creation for User Type
create view rdstaffreport.vw_user_type as
select user_type_id, description
from dbrdcaseworker.user_type;

-- view creation for Role Type
create view rdstaffreport.vw_role_type as
select role_id, description
from dbrdcaseworker.role_type;

-- view creation for skill
create view rdstaffreport.vw_skill as
select skill_id, skill_code, description, service_id, user_type
from dbrdcaseworker.skill;

-- view creation for case worker skill
create view rdstaffreport.vw_case_worker_skill as
select case_worker_id, skill_id
from dbrdcaseworker.case_worker_skill;
