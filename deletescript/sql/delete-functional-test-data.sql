
delete from  case_worker_location
where case_worker_id in (
 select cwp.case_worker_id from case_worker_profile  cwp
 where cwp.email_id  like (values('cwr-rd-func-test-user-only-%'))
 );

delete from  case_worker_work_area
where case_worker_id in (
 select cwp.case_worker_id from case_worker_profile  cwp
 where cwp.email_id  like (values('cwr-rd-func-test-user-only-%'))
 );

delete from  case_worker_role
where case_worker_id in (
 select cwp.case_worker_id from case_worker_profile  cwp
 where cwp.email_id  like (values('cwr-rd-func-test-user-only-%'))
);


delete from  case_worker_location
where case_worker_id in (
 select cwp.case_worker_id from case_worker_profile  cwp
 where cwp.email_id  like (values('staff-rd-profile-func-test-user-only-%'))
 );

delete from  case_worker_work_area
where case_worker_id in (
 select cwp.case_worker_id from case_worker_profile  cwp
 where cwp.email_id  like (values('staff-rd-profile-func-test-user-only-%'))
 );

delete from  case_worker_role
where case_worker_id in (
 select cwp.case_worker_id from case_worker_profile  cwp
 where cwp.email_id  like (values('staff-rd-profile-func-test-user-only-%'))
);

delete from case_worker_profile cwp where
      email_id like (values('cwr-rd-func-test-user-only-%'))
   or email_id like (values('staff-rd-profile-func-test-user-only-%')) ;

commit;
