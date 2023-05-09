delete from case_worker_profile cwp where
      email_id like any (values('cwr-rd-func-test-user-only%'))
   or email_id like any (values('cwr-func-test-user-%'))
   or email_id like any(values('staff-rd-profile-func-test-user-only-%')) ;

commit;