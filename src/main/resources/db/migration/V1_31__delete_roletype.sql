
delete from
    case_worker_idam_role_assoc
where
    role_id  ='16';

delete from
   case_worker_role cwr
where
   role_id ='16';

delete from
    role_type
where
    role_id = '16' and description = 'Registrar';
